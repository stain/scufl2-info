(ns scufl2-info.util
  ;(:import com.github.jsonldjava.jena.JenaJSONLD)
  )


(defn ensure-uuid [uuid]
   (try 
      (java.util.UUID/fromString uuid)
    (catch Exception e
      (throw (ex-info (str "Invalid UUID: " uuid) {:status 400})))))

(defn ensure-int [number]
  (try
    (Integer/parseInt number)
  (catch NumberFormatException e
    (throw (ex-info (str "Invalid integer: " number) {:status 400})))))

(defn ensure-bool [bool-str]
  (Boolean/parseBoolean bool-str))

; Initialize once
;(JenaJSONLD/init)

;(defn jsonld-to-turtle [json]
;  json
;  ;; TODO
;  )
  


