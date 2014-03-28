(ns scufl2-info.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.codec :as codec]
            [compojure.route :as route]))

(defn wfbundle-uri [uuid]
  (str "http://ns.taverna.org.uk/2010/workflowBundle/" (codec/url-encode uuid) "/"))

(defn workflow-uri [uuid workflow]
  ; Relative URI from wfbundle, as we set @base
  (str "workflow/" (codec/url-encode workflow) "/"))

(defn processor-uri [uuid workflow processor]
  (str (workflow-uri uuid workflow) "processor/" (codec/url-encode processor) "/"))

(defn processor-port-uri [uuid workflow processor inOrOut port]
  (str (processor-uri uuid workflow processor) (name inOrOut) "/" (codec/url-encode port)))

(defn workflow-port-uri [uuid workflow inOrOut port]
  (str (workflow-uri uuid workflow) (name inOrOut) "/" (codec/url-encode port)))

(defn iterationstrategy-uri [uuid workflow processor]
  (str (processor-uri uuid workflow processor) "iterationstrategy/"))

(defn datalink-uri [uuid workflow from to]
  (str (workflow-uri uuid workflow) "datalink?from=" from "&to=" to))

(defn wfbundle-json [uuid]
  { "@context" {
                "@base" (wfbundle-uri uuid)
                "rdfs" "http://www.w3.org/2000/01/rdf-schema#"
                "seeAlso" "rdfs:seeAlso"
                "@vocab" "http://ns.taverna.org.uk/2010/scufl2#"
                }
    ; Absolute URI here, because some people would be confused by "" or "."
    "@id" (wfbundle-uri uuid) 
    "@type" :WorkflowBundle})

(defn workflow-json [uuid workflow]
  (assoc (wfbundle-json uuid)
         :workflow { 
                    "@id" (workflow-uri uuid workflow)
                    "@type" :Workflow
                    :name workflow}))

(defn processor-json [uuid workflow processor]
  (assoc-in (workflow-json uuid workflow)
            [:workflow :processor]
            { "@id" (processor-uri uuid workflow processor)
              "@type" :Processor
              :name processor }))

(defn processor-port-json [uuid workflow processor inOrOut port]
  (assoc-in (processor-json uuid workflow processor)
            [:workflow :processor (case inOrOut 
                :in :inputProcessorPort
                :out :outputProcessorPort)]
            { "@id" (processor-port-uri uuid workflow processor inOrOut port)
              "@type" (case inOrOut 
                        :in :InputProcessorPort
                        :out :OutputProcessorPort)
              "name" port}))

(defn workflow-port-json [uuid workflow inOrOut port]
  (assoc-in (workflow-json uuid workflow)
            [:workflow (case inOrOut 
                :in :inputWorkflowPort
                :out :outputWorkflowPort)]
            { "@id" (workflow-port-uri uuid workflow inOrOut port)
              "@type" (case inOrOut 
                        :in :InputWorkflowPort
                        :out :OutputWorkflowPort)
              "name" port}))

(defn iteration-stack-json [uuid workflow processor]
  (assoc-in (processor-json uuid workflow processor)
            [:workflow :processor :iterationStrategyStack]
            { "@id" (iterationstrategy-uri uuid workflow processor)
              "@type" :IterationStrategyStack }))

(defn datalink-json [uuid workflow from to]
  (assoc-in (workflow-json uuid workflow)
            [:workflow :datalink]
            { "@id" (datalink-uri uuid workflow from to)
              "@type" :DataLink
             ; TODO: support merge
              :receiveFrom { "@id" (str (workflow-uri uuid workflow) from)
                             "@type" (if (.startsWith from "in/") :InputWorkflowPort :OutputProcessorPort)
                            }
              :sendTo { "@id" (str (workflow-uri uuid workflow) to) 
                             "@type" (if (.startsWith to "out/") :OutputWorkflowPort :InputProcessorPort)
                       } 
             ; TODO: Should we also expand from and to here to show ports and
             ; processors, or expect the client to simply follow the links?
             }))

(defn replace-second [coll new-second]
  (cons (first coll) 
        (cons new-second
              (rest (rest coll)))))

(defmacro check-uuid [call]
  "Replace the 'uuid' argument in call with a normalized UUID string, and wrap as { :body return }"
   `(try { :body ~(replace-second call `(str (java.util.UUID/fromString ~(second call)))) }
    (catch IllegalArgumentException ~'e { :status 400 :body (str "Invalid workflow bundle UUID: " ~(second call))})))


(defroutes app-routes
  (GET "/" [] "
              <h1>scufl2-info</h1>
              This is the <a href='https://github.com/stain/scufl2-info'>scufl2-info</a> web service.
              <p>
              This service generates <a href='http://json-ld.org/'>JSON-LD</a> Linked Data descriptions for 
              <a href='http://dev.mygrid.org.uk/wiki/display/developer/SCUFL2'>SCUFL2</a> resources,
              following the same URI syntax as below the base
              <code>http://ns.taverna.org.uk/2010/workflowBundle/</code>
              <p>
              Examples:
              <ul>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/'>workflow bundle</a></li>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/workflow/HelloWorld/'>workflow</a></li>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/workflow/HelloWorld/in/input1'>workflow input port</a></li>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/workflow/HelloWorld/processor/hello/'>processor</a></li>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/workflow/HelloWorld/processor/hello/out/output1'>processor output port</a></li>
                <li> <a href='workflowBundle/2f0e94ef-b5c4-455d-aeab-1e9611f46b8b/workflow/HelloWorld/datalink?from=processor/hello/out/output1&to=out/result'>datalink</a></li>
              </ul>
              <p>
              Questions? Contact support@mygrid.org.uk
              ")
  ; TODO: Check that uuid is a valid uuid, else 404 on all below
  (GET "/workflowBundle/:uuid/" 
       [uuid] (check-uuid (wfbundle-json uuid)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/" 
       [uuid workflow] (check-uuid (workflow-json uuid workflow))) 
  (GET "/workflowBundle/:uuid/workflow/:workflow/in/:port" 
       [uuid workflow processor port] (check-uuid (workflow-port-json uuid workflow :in port)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/out/:port" 
       [uuid workflow processor port] (check-uuid (workflow-port-json uuid workflow :out port)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/" 
       [uuid workflow processor] (check-uuid (processor-json uuid workflow processor)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/in/:port" 
       [uuid workflow processor port] (check-uuid (processor-port-json uuid workflow processor :in port)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/out/:port" 
       [uuid workflow processor port] (check-uuid (processor-port-json uuid workflow processor :out port)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/iterationstrategy/" 
       [uuid workflow processor] (check-uuid (iteration-stack-json uuid workflow processor)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/datalink"
       [uuid workflow from to] 
       (if (or (nil? from) (nil? to))
        { :status 404
          :body "Not Found.\ndatalink requires query parameters 'from' and 'to'" } 
       (check-uuid (datalink-json uuid workflow from to))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> 
    (handler/site app-routes)
    (middleware/wrap-json-response {:pretty true})))
