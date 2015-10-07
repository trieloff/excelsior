(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [taoensso.faraday :as far]
            [dk.ative.docjure.spreadsheet :as doc]
            [schema.core :as s]))

(s/defschema Message {:message String})
(s/defschema Spreadsheet {:input {s/Keyword (s/either Long String)}
                          :meta {:customer String :spreadsheet String :name String :id String :url String :type String :output #{String} :inputs #{String}}
                          :output {s/Keyword (s/either Long String)}})

(def client-opts
  {;;; For DDB Local just use some random strings here, otherwise include your
   ;;; production IAM keys:
   :access-key "<AWS_DYNAMODB_ACCESS_KEY>"
   :secret-key "<AWS_DYNAMODB_SECRET_KEY>"

   ;;; You may optionally override the default endpoint if you'd like to use DDB
   ;;; Local or a different AWS Region (Ref. http://goo.gl/YmV80o), etc.:
   :endpoint "http://localhost:8000"                   ; For DDB Local
   ;; :endpoint "http://dynamodb.eu-west-1.amazonaws.com" ; For EU West 1 AWS region
   })

(def crypt-opts {:password [:salted "foobar"]})

(far/ensure-table client-opts :spreadsheets
                  [:id :s]  ; Primary key named "id", (:n => number type)
                  {:throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                   :block? true ; Block thread during table creation
                   })


(far/list-tables client-opts)

(far/put-item client-opts
              :spreadsheets
              {:id "hans/help"
               :name "example.xls"
               :url "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
               :type "local"
               :inputs (far/freeze #{"A1", "B1"} crypt-opts)
               :output (far/freeze #{"C1" "C2"} crypt-opts)})

(far/get-item client-opts
              :spreadsheets
              {:id "0"})

(def formula-from-sheet
  (doc/cell-fn "C1"
               (first (doc/sheet-seq (doc/load-workbook "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx")))
               "A1"))

(formula-from-sheet "Hund")

(def inputs #{"A2" "A1"})
(def outputs #{"C2" "C1"})
(def params {:A2 12 :A1 "Katze"})

(defn sheet-formulas [inputs outputs sheet]
  (into (hash-map) (map #(conj
                          (vector (keyword %))
                          (apply doc/cell-fn % (first (doc/sheet-seq (doc/load-workbook sheet))) (sort inputs))) (sort outputs))))

(defapi app
  (swagger-ui)
  (swagger-docs
    {:info {:title "Excelsior"
            :description "A REST API for Spreadsheets"}
     :tags [{:name "hello", :description "says hello in Finnish"}
            {:name "formula" :description "evaluates Excel spreadsheet formulas"}]})
  (context* "/formula" []
            :tags ["formula"]
            (GET* "/:customer/:spreadsheet" request
                  :return Spreadsheet
                  :path-params [customer :- String spreadsheet :- String]
                  :summary "calculate the response value"
                  (ok (let
                        [meta (far/with-thaw-opts crypt-opts (far/get-item client-opts
                                            :spreadsheets {:id (str customer "/" spreadsheet)}))
                         inputs   (:inputs meta)
                         outputs  (:output meta)
                         params   (:params request)
                         sheet    (:url meta)
                         fnmap    (sheet-formulas inputs outputs sheet)
                         values   (vals (into (sorted-map) params))]
                        {:input (:params request)
                       :meta (merge
                              {:customer customer :spreadsheet spreadsheet }
                              meta)
                       :output (zipmap (keys fnmap) (map #(apply % values) (vals fnmap)))}))))
  (context* "/hello" []
    :tags ["hello"]
    (GET* "/" []
      :return Message
      :query-params [name :- String]
      :summary "say hello"
      (ok {:message (str "Terve, " name)}))))
