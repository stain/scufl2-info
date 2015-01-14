(ns scufl2-info.cgibin
  (:gen-class)
  (:require
    [ring.mock.request :as mock]
    [scufl2-info.handler :as handler]
    ))


(defn -main [& args]

  (let [path (or (System/getenv "PATH_INFO") "/")
        method (or (System/getenv "REQUEST_METHOD") "GET")
        request (mock/request (keyword (.toLowerCase method)) path)
        ; TODO - all the HTTP_ environment variables
        ; should be translated into HTTP headers, e.g.
        ;HTTP_USER_AGENT=Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0
        ; into
        ; :headers {"user-agent" "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0" }
        response (handler/app request)]



  (println "Content-Type: text/plain")
  (println "")

  (println "This was made by Clojure")
  (println path)
  (println args)
  (println request)
  (println response)
  ))
