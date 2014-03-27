(ns scufl2-info.test.handler
  (:use clojure.test
        ring.mock.request  
        cheshire.core 
        scufl2-info.handler))

(defn test-workflow-bundle [wfbundle]
    (is (not (nil? wfbundle)))
    (is (= (get wfbundle "@id") "http://ns.taverna.org.uk/2010/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/" ))
    (is (= (get wfbundle "@type") "WorkflowBundle"))
    (is (contains? wfbundle "@context"))
    (is (= (get-in wfbundle ["@context" "@vocab"]) "http://ns.taverna.org.uk/2010/scufl2#"))
    (is (= (get-in wfbundle ["@context" "@base"]) "http://ns.taverna.org.uk/2010/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/")))

(defn test-workflow [workflow]
  (is (not (nil? workflow)))
  (is (= (get workflow "name") "HelloWorld"))
  (is (= (get workflow "@id") "workflow/HelloWorld/"))
  (is (= (get workflow "@type") "Workflow")))

(defn test-processor [processor]
  (is (not (nil? processor)))
  (is (= (get processor "name") "hello"))
  (is (= (get processor "@id") "workflow/HelloWorld/processor/hello/"))
  (is (= (get processor "@type") "Processor")))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "workflow bundle"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json))))
  
  (testing "workflow"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow")))))

  (testing "processor"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/processor/hello/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-processor (get-in json ["workflow" "processor"])))))

  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
