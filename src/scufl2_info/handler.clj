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

(defn iteration-stack-json [uuid workflow processor]
  (assoc-in (processor-json uuid workflow processor)
            [:workflow :processor :iterationStrategyStack]
            { "@id" (iterationstrategy-uri uuid workflow processor)
              "@type" :IterationStrategyStack }))

(defn datalink-json [uuid workflow from to]
  (str "Datalink " from " --> " to))


(defn replace-second [coll new-second]
  (cons (first coll) 
        (cons new-second
              (rest (rest coll)))))

(defmacro check-uuid [call]
  "Replace the 'uuid' argument in call with a normalized UUID string, and wrap as { :body return }"
   `(try { :body ~(replace-second call `(str (java.util.UUID/fromString ~(second call)))) }
    (catch IllegalArgumentException ~'e { :status 400 :body (str "Invalid workflow bundle UUID: " ~(second call))})))


(defroutes app-routes
  (GET "/" [] "Hello World")
  ; TODO: Check that uuid is a valid uuid, else 404 on all below
  (GET "/workflowBundle/:uuid/" 
       [uuid] (check-uuid (wfbundle-json uuid)))
  (GET "/workflowBundle/:uuid/workflow/:workflow/" 
       [uuid workflow] (check-uuid (workflow-json uuid workflow))) 
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
