(ns scufl2-info.jsonld
  (:import com.github.jsonldjava.jena.JenaJSONLD)
  ;(:import com.github.jsonldjava.utils.JsonUtils)
  (:import com.github.jsonldjava.utils.JSONUtils)
  (:import org.apache.jena.riot.RDFDataMgr)
  (:import org.apache.jena.riot.Lang)
  (:import com.hp.hpl.jena.rdf.model.ModelFactory)
  (:import java.io.StringReader)
  (:import java.io.StringWriter)
  (:require [cheshire.core :as json]
            ))


; Initialize Jena binding once -- is this needed as long as we do import?
;(JenaJSONLD/init)

(defn jsonld-to-turtle [json]
  ;; We need a mutable Java version :-(
  (let [json (JSONUtils/fromString (json/generate-string (str json)))
        reader (StringReader. json)
        writer (StringWriter. )
        model (ModelFactory/createDefaultModel)]
    (RDFDataMgr/read model reader "" JenaJSONLD/JSONLD)
    (RDFDataMgr/write writer model Lang/TURTLE)
    (str writer)))


