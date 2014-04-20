(ns scufl2-info.jsonld
  (:import com.github.jsonldjava.jena.JenaJSONLD)
  ;(:import com.github.jsonldjava.utils.JsonUtils)
  ;(:import com.github.jsonldjava.utils.JSONUtils)
  (:import org.apache.jena.riot.RDFDataMgr)
  (:import org.apache.jena.riot.Lang)
  (:import org.apache.jena.riot.RDFLanguages)
  (:import com.hp.hpl.jena.rdf.model.ModelFactory)
  (:import com.hp.hpl.jena.query.DatasetFactory)
  (:import java.io.StringReader)
  (:import java.io.StringWriter)
  (:import java.io.ByteArrayInputStream)
  (:require [cheshire.core :as json]
            ))


; Initialize Jena binding once -- is this needed as long as we do import?
(JenaJSONLD/init)

(defn jsonld-to-rdf [json lang]
  (let [jsonstr  (json/generate-string json)
        stream (ByteArrayInputStream. (.getBytes jsonstr "UTF-8"))
        writer (StringWriter. )
        base  "app://6b16aa40-ae2a-4fbc-9c8d-321464f03f3d/"
        dataset (DatasetFactory/createMem)
        lang (RDFLanguages/nameToLang (name lang))
        ;model (ModelFactory/createDefaultModel)
    ]
    (if (= JenaJSONLD/JSONLD lang) json ; return as is
      ; need to convert
      (do 
        (RDFDataMgr/read dataset stream base JenaJSONLD/JSONLD)
        (if (RDFLanguages/isQuads lang)
          (RDFDataMgr/write writer dataset lang)
          (RDFDataMgr/write writer (.getDefaultModel dataset) lang))
        (str writer)))))



(defn content-types-of-lang [lang]
  (conj (seq (.getAltContentTypes lang)) (.getContentType (.getContentType lang))))

; lazy-mapcat by Benny Tsal 2011-08-22
; https://groups.google.com/d/msg/clojure/vzhFmpGkWTo/SAC-lwzDI8cJ
; https://groups.google.com/forum/#!topic/clojure/vzhFmpGkWTo
;(defn- lazy-mapcat [f & colls]
;  (lazy-seq
;   (when (every? seq colls)
;     (concat (apply f (map first colls))
;             (apply lazy-mapcat f (map rest colls))))))

(def rdf-content-types
  ;(distinct (lazy-mapcat 
  ; Sorry, can't do a lazy-set as it won't work with (contains?)
  (set (mapcat 
              content-types-of-lang (RDFLanguages/getRegisteredLanguages))))


