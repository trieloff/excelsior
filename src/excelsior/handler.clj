(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring-aws-lambda-adapter.core :refer [defhandler]]
            [ring.util.http-response :refer :all]
            [dk.ative.docjure.spreadsheet :as doc]
            [excelsior.core :as env]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [schema.core :as s]))

(defn -init []
  (println "Database Initialization")

  (println "Access key: " (env/env :aws-access-key)))

(def init (memoize -init))

(def aws-gateway-options {
                           :x-amazon-apigateway-integration {
                                                              :responses { :default { :statusCode "200"
                                                                                      :responseTemplates { "application/json" "$input.json('$.body')" }}
                                                                           }
                                                              :requestTemplates { "application/json" (slurp (io/resource "bodymapping.vm")) }
                                                              :uri (str "arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/" (env/env :lambda-arn) "/invocations")
                                                              ;:credentials "arn:aws:iam::320028119408:role/lambda-role"
                                                              :httpMethod "POST"
                                                              :type "aws" }})


(defn clean-values [value]
  (println value)
  (case (:type value)
    "rating" (:number value)
    "choice" (-> value :choice :label)
    ((-> value :type keyword) value)))

(defapi app
  {:ring-swagger {:ignore-missing-mappings? true}
   :swagger
   {:ui "/"
    :spec "/swagger.json"
    :data {:info {:title "Excelsior"
            :description "A REST API for Spreadsheets"}}}}
  (context "/calculation" []
           (POST "/:sheet/:cell" request
                 :return s/Any
                 :query-params [spreadsheet :- String]
                 :path-params [sheet :- Long cell :- String]
                 :summary "Calculate response value from WebHook"
                 :swagger aws-gateway-options
                 (let [body (parse-string (slurp (:body request)) true)
                       answer (-> body :form_response :answers)
                       inputs (filter #(re-matches #"[A-Z]+[0-9]+" %) (-> request :query-params keys))
                       cellfunc (apply doc/cell-fn cell (first (doc/sheet-seq (doc/load-workbook spreadsheet))) inputs)
                       ;cellfunc (doc/cell-fn cell (nth (doc/sheet-seq (doc/load-workbook spreadsheet)) sheet) inputs)
                       fieldids (map #(get (-> request :query-params) %) inputs)
                       values (map clean-values (map (fn [fieldid] (first (filter #(= (-> % :field :id) fieldid) answer))) fieldids))]
                   (ok {:spreadsheet spreadsheet
                        :inputs inputs
                        :values values
                        :return (apply cellfunc values)})))
          (GET "/" request
                :return s/Any
                :query-params [spreadsheet :- String]
                :summary "Calculate response value from Redirect"
                :swagger aws-gateway-options
                (ok {:message spreadsheet})))
  (context "/hello" []
    :tags ["hello"]
    (GET "/" []
      :return s/Any
      :query-params [name :- String]
      :summary "say hello"
      :swagger aws-gateway-options
      (ok {:message (str "Tere, " name)}))
    (POST "/" []
      :return s/Any
      :body-params [name :- String]
      :summary "say hello"
      :swagger aws-gateway-options
      (ok {:message (str "Hallo, " name)}))))

(defhandler excelsior.handler.Lambda app {})
