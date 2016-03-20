(defproject excelsior "0.1.0-SNAPSHOT"
  :description "Turns Excel into HTTP"
  :url "https://github.com/trieloff/excelsior"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [metosin/compojure-api "1.0.1"]
                 [com.taoensso/faraday "1.9.0-beta1"]
                 [environ "1.0.2"]
                 [ring-aws-lambda-adapter "0.1.1"]
                 [dk.ative/docjure "1.11.0-SNAPSHOT"]]
  :ring {:handler excelsior.handler/app}
  :uberjar-name "server.jar"
  :resource-paths ["resources"
                   ".lein-env"] ; let's see if we can sneak the environment variables into the binary
  :plugins [[test2junit "1.1.2"]
            [lein-environ "1.0.2"]
            [lein-maven-s3-wagon "0.2.5"]]
  :test2junit-output-dir ~(or (System/getenv "CIRCLE_TEST_REPORTS") "target/test2junit")
  :env {:aws-access-key #=(eval (System/getenv "AWS_ACCESS_KEY"))
        :aws-secret-key #=(eval (System/getenv "AWS_SECRET_KEY"))
        :dynamodb-crypt-key #=(eval (System/getenv "DYNAMODB_CRYPT_KEY"))
        :dynamodb-endpoint #=(eval (System/getenv "DYNAMODB_ENDPOINT"))}
  :deploy-repositories {"private" {:url "s3://leinrepo/releases/"
                                   :username :env/aws_access_key ;; gets environment variable AWS_ACCESS_KEY
                                   :password :env/aws_secret_key}}
  :repositories {"private" {:url "s3://leinrepo/releases/"
                            :username :env/aws_access_key ;; gets environment variable AWS_ACCESS_KEY
                            :password :env/aws_secret_key}}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.7"]]}
             :uberjar {:main excelsior.core :aot :all}})
