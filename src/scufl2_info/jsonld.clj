(ns scufl2-info.jsonld
  (:import com.github.jsonldjava.jena.JenaJSONLD)
  ;(:import com.github.jsonldjava.utils.JsonUtils)
  ;(:import com.github.jsonldjava.utils.JSONUtils)
  (:import org.apache.jena.riot.RDFDataMgr)
  (:import org.apache.jena.riot.Lang)
  (:import com.hp.hpl.jena.rdf.model.ModelFactory)
  (:import java.io.StringReader)
  (:import java.io.StringWriter)
  (:import java.io.ByteArrayInputStream)
  (:require [cheshire.core :as json]
            ))


; Initialize Jena binding once -- is this needed as long as we do import?
(JenaJSONLD/init)

(defn jsonld-to-turtle [json]
  (let [jsonstr  (json/generate-string json)
        stream (ByteArrayInputStream. (.getBytes jsonstr "UTF-8"))
        writer (StringWriter. )
        base  "app://6b16aa40-ae2a-4fbc-9c8d-321464f03f3d/"
        model (ModelFactory/createDefaultModel)]
    (RDFDataMgr/read model stream base JenaJSONLD/JSONLD)
    (RDFDataMgr/write writer model Lang/TURTLE)
    (str writer)))


