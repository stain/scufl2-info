(ns scufl2-info.run
  (:use compojure.core)
  (:use scufl2-info.util)
  (:require
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as response]
            [ring.util.codec :as codec]
            [compojure.route :as route]
            ))


(defn run-uri [run]
  (str "http://ns.taverna.org.uk/2011/run/" (normalize-uuid run) "/"))

(defn process-uri [run process]
  ; Relative URI from run, as we set @base
  (str "process/" (normalize-uuid process) "/"))

(defn jsonld-context [run]
  { "@context" {
                "@base" (run-uri run)
                ;"rdfs" "http://www.w3.org/2000/01/rdf-schema#"
                ;"seeAlso" { "@id" "rdfs:seeAlso"
                ;            "@type" "@id" }
                ;"label" "rdfs:label"
                "@vocab" "http://purl.org/wf4ever/wfprov#"
                "tavernaprov" "http://ns.taverna.org.uk/2012/tavernaprov/"
                "scufl2" "http://ns.taverna.org.uk/2010/scufl2#"
                }})

(defn run-json [run]
   {
    ; Absolute URI here, because some people would be confused by "" or "."
    "@id" (run-uri run) 
    "@type" :WorkflowRun
    "wasEnactedBy" { "@id" "_:tavernaEngine"
                     "@type" "tavernaprov:TavernaEngine" }
   })

(defn run-json-resource [run]  
   (or (uuid-test run)
        {:body    
          (merge 
              (run-json run)
              (jsonld-context run) ;; last -> on top
            )}))


(defn process-json [run process]
  { "@id" (process-uri run process)
    "@type" :ProcessRun
    "wasEnactedBy" { "@id" "_:tavernaEngine"
                     "@type" "tavernaprov:TavernaEngine" }
    "wasPartOfWorkflowRun" (run-json run)
    })

(defn process-json-resource [run process]  
   (or (uuid-test run)
       (uuid-test process)
       {:body 
        (merge 
          (process-json run process)
          (jsonld-context run) ;; last -> on top
        )}))
  


(def run-context (context "/run" []
  (GET "/" [] "
              <h1>scufl2-info run</h1>
              This is the <a href='https://github.com/stain/scufl2-info'>scufl2-info</a> web service.
              <p>
              This service generates <a href='http://json-ld.org/'>JSON-LD</a> Linked Data descriptions for 
              <a href='http://www.taverna.org.uk/'>Taverna workflow runs</a>
              as found in the provenance run bundles exported from Taverna.
              </p>
              <p>This service reflects URIs under the namespace
              <code>http://ns.taverna.org.uk/2011/run/</code>
              <p>
              Examples:
              <ul>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/'>workflow run</a></li>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/process/1e027057-2aeb-47f7-97dc-03e19e9772be/'>process run</a></li>
              </ul>
              <p>
              Questions? Contact support@mygrid.org.uk
              ")
  (context "/:run" [run] 
    ;; TODO: Check UUID here instead of using check-uuid macro?
    (GET "/" 
        [uuid] (run-json-resource run))
    (GET "/process/:process/" 
        [process] (process-json-resource run process)))))

