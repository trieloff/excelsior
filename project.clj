(defproject excelsior "0.1.0-SNAPSHOT"
  :description "Turns Excel into HTTP"
  :url "https://github.com/trieloff/excelsior"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [metosin/compojure-api "0.23.1"]
                 [com.taoensso/faraday "1.8.0"]
                 [dk.ative/docjure "1.11.0-SNAPSHOT"]]
  :ring {:handler excelsior.handler/app}
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.7"]]}})
