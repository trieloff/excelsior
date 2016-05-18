(ns excelsior.core-test
  (:require [clojure.test :refer :all]
            [org.httpkit.client :as http]
            [excelsior.core :refer :all]))

(deftest a-test
  (testing "I don't fail."
    (is (= 1 1))))

(use 'dk.ative.docjure.spreadsheet)

(-> (load-workbook "resources/helloworld.xlsx") (select-name "Sheet1"))

(let [{:keys [status headers body error] :as string}
        @(http/get "https://s3.amazonaws.com/excelsior-spreadsheets/helloworld.xlsx")]
    (select-cell "A1" (first (sheet-seq (load-workbook body)))))
