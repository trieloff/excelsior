(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring-aws-lambda-adapter.core :refer [defhandler]]
            [ring.util.http-response :refer :all]
            [dk.ative.docjure.spreadsheet :as doc]
            [org.httpkit.client :as http]
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
  (case (:type value)
    "rating" (:number value)
    "choice" (-> value :choice :label)
    ((-> value :type keyword) value)))

(defn read-values [value]
  (try (Float. value)
    (catch Exception e
      (cond
        (re-matches #"(?i)true" value) true
        (re-matches #"(?i)false" value) false
        :default value))))

(defn calculate [spreadsheet-url cell inputs values]
  (let [{:keys [status headers body error] :as string}
                 @(http/get spreadsheet-url)]
             (if (not (= status 200))
               {:status status
                :error (if error error "Unknown error")}
               (let [cellfunc (apply doc/cell-fn cell (first (doc/sheet-seq (doc/load-workbook body))) inputs)]
               {:value (apply cellfunc values)
                :staus status}))))

(defn continue-with [body urls]
  (pmap #(http/post % {:body (generate-string body)}) urls))

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
                 :query-params [spreadsheet :- String continue :- [String]]
                 :path-params [sheet :- Long cell :- String]
                 :summary "Calculate response value from WebHook"
                 :swagger aws-gateway-options
                 (let [body (parse-string (slurp (:body request)) true)
                       answer (-> body :form_response :answers)
                       inputs (filter #(re-matches #"[A-Z]+[0-9]+" %) (-> request :query-params keys))
                       fieldids (map #(get (-> request :query-params) %) inputs)
                       values (map clean-values (map (fn [fieldid] (first (filter #(= (-> % :field :id) fieldid) answer))) fieldids))
                       calculation (calculate spreadsheet cell inputs values)
                       output (assoc body :calculation calculation)
                       continuation (continue-with output continue)]
                   (if (:error calculation)
                     (not-found output)
                     (ok output))))
          (GET "/:sheet/:cell" request
                :return s/Any
                :query-params [spreadsheet :- String continue :- String]
                :path-params [sheet :- Long cell :- String]
                :summary "Calculate response value from Redirect"
                :swagger aws-gateway-options
                (let [body (parse-string (slurp (:body request)) true)
                       inputs (filter #(re-matches #"[A-Z]+[0-9]+" %) (-> request :query-params keys))
                       values (map read-values (map #(get (-> request :query-params) %) inputs))
                       calculation (calculate spreadsheet cell inputs values)
                       ;output (assoc body :calculation calculation)
                       ;continuation (continue-with output continue)
                      ]
                   (found (str continue (ring.util.codec/form-encode(assoc
                                    (apply assoc {} (interleave inputs values))
                                    :value (:value calculation))))))))
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
