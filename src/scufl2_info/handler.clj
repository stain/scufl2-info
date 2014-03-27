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

(defn iterationstrategy-uri [uuid workflow processor]
  (str (processor-uri uuid workflow processor) "iterationstrategy/"))

(defn wfbundle-json [uuid]
  { "@context" {
                "@base" (wfbundle-uri uuid)
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

(defn iteration-stack [uuid workflow processor]
  (assoc-in (processor-json uuid workflow processor)
            [:workflow :processor :iterationStrategyStack]
            { "@id" (iterationstrategy-uri uuid workflow processor)
              "@type" :IterationStrategyStack }))


(defroutes app-routes
  (GET "/" [] "Hello World")
  ; TODO: Check that uuid is a valid uuid, else 404 on all below
  (GET "/workflowBundle/:uuid/" 
       [uuid] {:body (wfbundle-json uuid)})
  (GET "/workflowBundle/:uuid/workflow/:workflow/" 
       [uuid workflow] {:body (workflow-json uuid workflow)}) 
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/" 
       [uuid workflow processor] {:body (processor-json uuid workflow processor)})
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/in/:port" 
       [uuid workflow processor port] {:body (processor-port-json uuid workflow processor :in port)}) 
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/out/:port" 
       [uuid workflow processor port] {:body (processor-port-json uuid workflow processor :out port)}) 
  (GET "/workflowBundle/:uuid/workflow/:workflow/processor/:processor/iterationstrategy/" 
       [uuid workflow processor] {:body (iteration-stack uuid workflow processor)})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> 
    (handler/site app-routes)
    (middleware/wrap-json-response {:pretty true})))
