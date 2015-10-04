(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Message {:message String})
(s/defschema Spreadsheet {:input {s/Keyword (s/either Long String)}
                          :meta {:customer String :spreadsheet String}
                          :output {s/Keyword (s/either Long String)}})

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
                  (ok {:input (:params request) :meta {:customer customer :spreadsheet spreadsheet} :output {:request (str (:params request))}})))
  (context* "/hello" []
    :tags ["hello"]
    (GET* "/" []
      :return Message
      :query-params [name :- String]
      :summary "say hello"
      (ok {:message (str "Terve, " name)}))))
