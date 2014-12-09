(defproject scufl2-info "0.4.1-SNAPSHOT"
  :description "SCUFL2 information service"
  :url "https://github.com/stain/scufl2-info"
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [com.github.jsonld-java/jsonld-java-jena "0.3"]
                 [ring-middleware-format "0.3.2"]
                 [ring/ring-json "0.3.1"]
                 [com.gfredericks/catch-data "0.1.3"]
                 [compojure "1.1.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler scufl2-info.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
