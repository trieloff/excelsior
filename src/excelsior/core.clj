(ns excelsior.core
  (:gen-class)
  (:require [environ.core :as env]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn env [x]
  "Returns configuration values from the environment, with a fallback to a .lein-env file in the
  classpath. This makes it possible to run without any configuration."
  (try
    (or (env/env x) (x (edn/read-string (slurp (io/resource ".lein-env")))))
    (catch java.lang.IllegalArgumentException e (env/env x))))
;;   (->> ".lein-env"
;;      io/resource
;;      slurp
;;      edn/read-string
;;      (map x (env/env x))))

(env/env :home)

(or nil 1)

(defn -main []
  "I don't do a whole lot."
  (println "Hello, World!")

  (println (env :dynamodb-endpoint)))
