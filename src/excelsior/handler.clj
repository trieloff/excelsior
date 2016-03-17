(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring-aws-lambda-adapter.core :refer [defhandler]]
            [ring.util.http-response :refer :all]
            [taoensso.faraday :as far]
            [clojure.string :as str]
            [dk.ative.docjure.spreadsheet :as doc]
            [ring.swagger.json-schema :as js]
            [environ.core :as env]
            [schema.core :as s]))

(s/defschema Message {:message String})

(s/defschema Meta {:customer String
                   :spreadsheet String
                   :name String
                   :url String
                   :type String
                   :output #{String}
                   :inputs #{String}})

(s/defschema Spreadsheet {:input {s/Keyword (s/either Long String)}
                          :meta Meta
                          :output {s/Keyword (s/either Number String)}})

(def client-opts
  {;;; For DDB Local just use some random strings here, otherwise include your
   ;;; production IAM keys:
   :access-key (env/env :aws-access-key) ;; reading from $AWS_ACCESS_KEY environment variable
   :secret-key (env/env :aws-secret-key) ;; reading from $AWS_SECRET_KEY environment variable

   ;;; You may optionally override the default endpoint if you'd like to use DDB
   ;;; Local or a different AWS Region (Ref. http://goo.gl/YmV80o), etc.:
   :endpoint (env/env :dynamodb-endpoint)                   ; For DDB Local
   ;; :endpoint "http://dynamodb.eu-west-1.amazonaws.com" ; For EU West 1 AWS region
   })

(print "Starting server")
(print client-opts)

(def crypt-opts {:password [:salted (env/env :dynamodb-crypt-key "secret")]})

(far/ensure-table client-opts :customers
                  [:customer :s]
                  {:throughput {:read 1 :write 1}
                   :block? true})

(far/ensure-table client-opts :spreadsheets
                  [:customer :s]  ; Primary key named "id", (:n => number type)
                  {:throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                   :range-keydef [:spreadsheet :s]
                   :block? true ; Block thread during table creation
                   })

(defn is-unique-name? [customer name]
  (nil? (far/with-thaw-opts crypt-opts (far/get-item client-opts
                                             :spreadsheets
                                             {:customer customer :spreadsheet name}))))

(defn count-variants [customer name]
  (inc (count (filter #(re-matches (re-pattern (str "^" name "-\\d+")) (:spreadsheet %)) (far/with-thaw-opts crypt-opts (far/query client-opts
                                          :spreadsheets {:customer [:eq customer]
                                                         :spreadsheet [:begins-with (str name "-")]}))))))

(defn make-unique-name [customer name]
  (if (is-unique-name? customer name)
    name
    (str name "-" (count-variants customer name))))


(defn sheet-formulas [inputs outputs sheet]
  (into (hash-map) (map #(conj
                          (vector (keyword %))
                          (apply doc/cell-fn % (first (doc/sheet-seq (doc/load-workbook sheet))) (sort inputs))) (sort outputs))))

(defapi app
  {:ring-swagger {:ignore-missing-mappings? true}
   :swagger
   {:ui "/"
    :spec "/swagger.json"
    :data {:info {:title "Excelsior"
            :description "A REST API for Spreadsheets"}}}}
  (context "/formula" []
            (GET "/:customer/:spreadsheet" request
                  :return Spreadsheet
                  :path-params [customer :- String spreadsheet :- String]
                  :summary "calculate the response value"
                  (ok (let
                        [meta (far/with-thaw-opts crypt-opts (far/get-item client-opts
                                            :spreadsheets {:customer customer
                                                           :spreadsheet spreadsheet}))
                         inputs   (:inputs meta)
                         outputs  (:output meta)
                         params   (:params request)
                         sheet    (:url meta)
                         fnmap    (sheet-formulas inputs outputs sheet)
                         values   (vals (into (sorted-map) params))]
                        {:input (:params request)
                       :meta meta
                       :output (zipmap (keys fnmap) (map #(apply % values) (vals fnmap)))})))

           (POST "/:customer/:spreadsheet" []
                   :return Meta
                   :path-params [customer :- String spreadsheet :- String]
                   :form-params [inputs :- (js/field [String] {:collectionFormat "multi" :description "the cells that will be used as input. Use cell references such as A1"})
                                 outputs :- (js/field [String] {:collectionFormat "multi" :description "the cells that will be used as output. Use cell references such as A2"})]
                   :summary "Update input and output cells for a spreadsheet"
                   (ok (let [meta (far/with-thaw-opts crypt-opts (far/get-item client-opts
                                            :spreadsheets {:customer customer
                                                           :spreadsheet spreadsheet}))
                             new-plain (assoc meta :inputs (set inputs) :output (set outputs))
                             new-crypt (assoc meta
                                         :inputs (far/freeze (set inputs) crypt-opts)
                                         :output (far/freeze (set outputs) crypt-opts))
                             put (far/put-item client-opts
                                               :spreadsheets
                                               new-crypt)]
                         new-plain)))

           (POST "/:customer/" []
                   :return Meta
                   :path-params [customer :- String]
                   :form-params [inputs :- (js/field [String] {:collectionFormat "multi" :description "the cells that will be used as input. Use cell references such as A1"})
                                 outputs :- (js/field [String] {:collectionFormat "multi" :description "the cells that will be used as output. Use cell references such as A2"})
                                 name :- String]
                   :summary "Create a new spreadsheet"
                   (ok (let [meta {:customer     customer
                                   :spreadsheet  (make-unique-name customer name)
                                   :name         name
                                   :url          "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
                                   :type         "local"
                                   :inputs       (far/freeze (set inputs) crypt-opts)
                                   :output       (far/freeze (set outputs) crypt-opts)}
                             put (far/put-item client-opts
                                               :spreadsheets
                                               meta)]
                         (assoc meta :inputs (set inputs) :output (set outputs)))))
            (GET "/:customer" []
                  :return [String]
                  :path-params [customer :- String]
                  :summary "List all spreadsheets for a customer"
                  (ok (map #(:spreadsheet %) (far/with-thaw-opts crypt-opts (far/query client-opts
                                                                :spreadsheets {:customer [:eq customer]}))))))
  (context "/hello" []
    :tags ["hello"]
    (GET "/" []
      :return Message
      :query-params [name :- String]
      :summary "say hello"
      (ok {:message (str "Terve, " name)}))))

(defhandler excelsior.handler.Lambda app {})
