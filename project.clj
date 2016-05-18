(defproject excelsior "0.1.0-SNAPSHOT"
  :description "Turns Excel into HTTP"
  :url "https://github.com/trieloff/excelsior"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [metosin/compojure-api "1.1.0"]
                 [environ "1.0.2"]
                 [http-kit "2.1.19"]
                 [ring-aws-lambda-adapter "0.1.1"]
                 [dk.ative/docjure "1.11.0-SNAPSHOT"]]
  :ring {:handler excelsior.handler/app}
  :uberjar-name "server.jar"
  :resource-paths ["resources"
                   ".lein-env"] ; let's see if we can sneak the environment variables into the binary
  :plugins [[test2junit "1.1.2"]
            [lein-environ "1.0.2"]
            [lein-aws-api-gateway "1.10.68-1"]
            [lein-clj-lambda "0.4.0"]
            [lein-maven-s3-wagon "0.2.5"]]
  :api-gateway {:api-id "n0mn6xmgo7"
                :swagger "target/swagger.json"}

  :test2junit-output-dir ~(or (System/getenv "CIRCLE_TEST_REPORTS") "target/test2junit")
  :env {:aws-access-key #=(eval (System/getenv "AWS_ACCESS_KEY"))
        :aws-secret-key #=(eval (System/getenv "AWS_SECRET_KEY"))
        :dynamodb-crypt-key #=(eval (System/getenv "DYNAMODB_CRYPT_KEY"))
        :dynamodb-endpoint #=(eval (System/getenv "DYNAMODB_ENDPOINT"))}
  :deploy-repositories {"private" {:url "s3://leinrepo/releases/"
                                   :username :env/aws_access_key ;; gets environment variable AWS_ACCESS_KEY
                                   :password :env/aws_secret_key}}
  :lambda {"dev" [{:handler "excelsior.handler.Lambda"
                  :memory-size 512
                  :timeout 300
                  :function-name "excelsior-dev"
                  :region "us-east-1"
                  :s3 {:bucket "leinrepo"
                       :object-key "excelsior-dev.jar"}}]
         "production" [{:handler "excelsior.handler.Lambda"
                        :memory-size 512
                        :timeout 300
                        :function-name "excelsior-prod"
                        :region "us-east-1"
                        :s3 {:bucket "leinrepo"
                            :object-key "excelsior-release.jar"}}]}

  :repositories {"private" {:url "s3://leinrepo/releases/"
                            :username :env/aws_access_key ;; gets environment variable AWS_ACCESS_KEY
                            :password :env/aws_secret_key}}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.7"]
                             [lein-dynamodb-local "0.2.8"]]}
             :uberjar {:main excelsior.core :aot :all}})
