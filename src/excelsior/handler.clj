(ns excelsior.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Message {:message String})
(s/defschema Calculation {:input String
                          :output {s/Keyword (s/either Long String)}})

(defapi app
  (swagger-ui)
  (swagger-docs
    {:info {:title "Excelsior"
            :description "Compojure Api example"}
     :tags [{:name "hello", :description "says hello in Finnish"}
            {:name "formula" :description "evaluates Excel spreadsheet formulas"}]})
  (context* "/formula" []
            :tags ["formula"]
            (GET* "/" []
                  :return Calculation
                  :query-params [input :- String]
                  :summary "calculate the response value"
                  (ok {:input input :output {(keyword input) 1.0}})))
  (context* "/hello" []
    :tags ["hello"]
    (GET* "/" []
      :return Message
      :query-params [name :- String]
      :summary "say hello"
      (ok {:message (str "Terve, " name)}))))
