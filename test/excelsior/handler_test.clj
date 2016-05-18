(ns excelsior.handler-test
  (:require [clojure.test :refer :all]
            [dk.ative.docjure.spreadsheet :as doc]
            [excelsior.handler :refer :all]
            [cheshire.core :refer :all]
            [clojure.walk :refer :all]))

(def formula-from-sheet
  (doc/cell-fn "C1"
               (first (doc/sheet-seq (doc/load-workbook "resources/helloworld.xlsx")))
               "A1"))

(def inputs #{"A2" "A1"})
(def outputs #{"C2" "C1"})
(def params {:A2 12 :A1 "Katze"})

(deftest swagger
  (is (not (nil? app)))
  (is (not (nil? (app {:request-method :get, :uri "/swagger.json"}))))
  (spit "target/swagger.json" (generate-string (postwalk #(if (and (map? %) (get % "default") (= (get % "default") {"description" ""}))
                                                            (assoc % 200 {:description "default response"})
                                                            %) (parse-string (slurp (:body (app {:request-method :get, :uri "/swagger.json"}))))))))

;(spit "target/swagger.json" (slurp (:body (app {:request-method :get, :uri "/swagger.json"}))))
;(generate-string (postwalk #(if (and (map? %) (get % "default")) (assoc % 200 {:description "default response"}) %) (parse-string (slurp (:body (app {:request-method :get, :uri "/swagger.json"}))))))

;http://requestb.in/16c4oj21?spreadsheet=/foo/far&A2={{answer_22581205}}&B2={{answer_22581235}}
