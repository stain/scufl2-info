(defproject scufl2-info "0.2.0"
  :description "SCUFL2 information service"
  :url "https://github.com/stain/scufl2-info"
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 ;; FIXME: These dependencies should have been found 
                 ;; transitively!!
                 [org.clojure/tools.reader "0.7.3"]
                 [commons-fileupload/commons-fileupload "1.3"]
                 [ring/ring-json "0.3.0"]
                 [compojure "1.1.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler scufl2-info.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
