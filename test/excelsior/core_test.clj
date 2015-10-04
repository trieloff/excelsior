(ns excelsior.core-test
  (:require [clojure.test :refer :all]
            [excelsior.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(use 'dk.ative.docjure.spreadsheet)

(-> (load-workbook "resources/helloworld.xlsx") (select-name "Sheet1"))
