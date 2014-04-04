(ns scufl2-info.data
  (:use compojure.core)
  (:use scufl2-info.util)
  (:require
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as response]
            [ring.util.codec :as codec]
            [compojure.route :as route]
            ))


(defn data-uri [data]
  (str "http://ns.taverna.org.uk/2011/data/" (ensure-uuid data) "/"))

(defn run-uri [uuid]
  (str "http://ns.taverna.org.uk/2011/run/" (ensure-uuid uuid) "/"))

(defn ref-uri [data reference]
  ; Relative URI from data, as we set @base
  (str (data-uri data) "ref/" (ensure-uuid reference) "/"))

(defn jsonld-context []
  { "@context" {
                "@vocab" "http://purl.org/wf4ever/wfprov#"
                "tavernaprov" "http://ns.taverna.org.uk/2012/tavernaprov/"
                "prov" "http://www.w3.org/ns/prov#"
                ; TODO: define new subproperty in tavernaprov
                "involvedInRun" { "@id" "tavernaprov:involvedInRun"
                                  "@type" "@id" }
                "depth" "tavernaprov:depth"
                "scufl2" "http://ns.taverna.org.uk/2010/scufl2#"
                }})

(defn ref-json [data reference]
   {
    "@id" (ref-uri data reference) 
    "@type" :Artifact
    "involvedInRun" (run-uri data)
   })

(defn error-json [data reference depth]
   {
    "@id" (ref-uri data reference) 
    "@type" tavernaprov:Error
    "depth" (int depth)
    "involvedInRun" (run-uri data)
   })


(defn ref-json-resource [data reference]  
    {:body    
      (merge 
          (ref-json data reference)
          (jsonld-context) ;; last -> on top
        )})

(defn error-json-resource [data reference depth]
  {:body    
    (merge 
        (error-json data reference)
        (jsonld-context) ;; last -> on top
      )})



(def data-context (context "/data" []
  (GET "/" [] "
              <h1>scufl2-info data</h1>
              This is the <a href='https://github.com/stain/scufl2-info'>scufl2-info</a> web service.
              <p>
              This service generates <a href='http://json-ld.org/'>JSON-LD</a> Linked Data descriptions for 
              <a href='http://www.taverna.org.uk/'>Taverna workflow data</a> resources
              as found in the provenance data bundles exported from Taverna.
              </p>
              <p>This service reflects URIs under the namespace
              <code>http://ns.taverna.org.uk/2011/data/</code>
              <p>
              Examples:
              <ul>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/ref/a060702f-7962-4773-9be3-99c026dd5da5/'>data artifact</a></li>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/list/c2f58d3e-8686-40a5-b1cd-b797cd18fbb7/false/1'>list (depth 1)</a></li>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/list/2cdc8e4c-ebcf-4662-83db-85a2fd496ca7/true/2'>list (depth 2, w/errors)</a></li>
                <li> <a href='d5ee659e-e11e-43a5-bc0a-58d93674e5e2/error/49785ed7-eda9-4d53-b74f-6c6a7a4940a5/0'>error (depth 0)</a></li>
              </ul>
              <p>
              Questions? Contact support@mygrid.org.uk
              ")
  (context "/:data" [data] 
    (GET "/" 
        [uuid] (response/redirect (run-uri data)))
    (GET "/ref/:reference/" 
        [reference] (ref-json-resource data reference))
           
    (GET "/error/:reference/:depth" 
        [reference depth] (error-json-resource data reference depth))
           
           )))

