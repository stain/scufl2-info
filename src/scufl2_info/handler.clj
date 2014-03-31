(ns scufl2-info.handler
  (:use compojure.core)
  (:require [scufl2-info.workflow-bundle :as wfbundle]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.codec :as codec]
            [compojure.route :as route]))

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
  (context "/workflowBundle/"
    wfbundle/routes)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> 
    (handler/site app-routes)
    (middleware/wrap-json-response {:pretty true})))
