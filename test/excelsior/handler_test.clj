(ns excelsior.handler-test
  (:require [clojure.test :refer :all]
            [taoensso.faraday :as far]
            [dk.ative.docjure.spreadsheet :as doc]
            [excelsior.handler :refer :all]))

(far/describe-table client-opts :spreadsheets)


(far/list-tables client-opts)

(far/put-item client-opts
              :customers
              {:customer "hans"
               :name (far/freeze "Hans Wurst" crypt-opts)})

(far/put-item client-opts
              :spreadsheets
              {:customer "hans"
               :spreadsheet "help"
               :name "example.xls"
               :url "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
               :type "local"
               :inputs (far/freeze #{"A1", "B1"} crypt-opts)
               :output (far/freeze #{"C1" "C2"} crypt-opts)})

(far/put-item client-opts
              :spreadsheets
              {:customer "hans"
               :spreadsheet "nase"
               :name "example.xls"
               :url "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
               :type "local"
               :inputs (far/freeze #{"A1", "B1"} crypt-opts)
               :output (far/freeze #{"D1" "D2"} crypt-opts)})

(far/put-item client-opts
              :spreadsheets
              {:customer "hans"
               :spreadsheet "nase-1"
               :name "example.xls"
               :url "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
               :type "local"
               :inputs (far/freeze #{"A1", "B1"} crypt-opts)
               :output (far/freeze #{"D1" "D2"} crypt-opts)})

(far/put-item client-opts
              :spreadsheets
              {:customer "hans"
               :spreadsheet "nase-ring"
               :name "example.xls"
               :url "/Users/ltrieloff/Documents/excelsior/resources/helloworld.xlsx"
               :type "local"
               :inputs (far/freeze #{"A1", "B1"} crypt-opts)
               :output (far/freeze #{"D1" "D2"} crypt-opts)})

(far/get-item client-opts :customers {:customer "hans"})


(count (far/with-thaw-opts crypt-opts (far/query client-opts
                                          :spreadsheets {:customer [:eq "hans"]
                                                         :spreadsheet [:begins-with "nase"]})))

(is-unique-name? "hans" "wurst")
(is-unique-name? "hans" "nase")
(count-variants "hans" "nase")
(make-unique-name "hans" "wurst")
(make-unique-name "hans" "nase")

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
  (spit "target/swagger.json" (slurp (:body (app {:request-method :get, :uri "/swagger.json"})))))
