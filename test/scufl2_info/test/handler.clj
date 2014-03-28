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

(defn test-input-processor-port [port]
  (is (not (nil? port)))
  (is (= (get port "name") "name"))
  (is (= (get port "@id") "workflow/HelloWorld/processor/hello/in/name"))
  (is (= (get port "@type") "InputProcessorPort")))

(defn test-output-processor-port [port]
  (is (not (nil? port)))
  (is (= (get port "name") "greeting"))
  (is (= (get port "@id") "workflow/HelloWorld/processor/hello/out/greeting"))
  (is (= (get port "@type") "OutputProcessorPort")))

(defn test-iteration-strategy-stack [strategy]
  (is (not (nil? strategy)))
  (is (= (get strategy "@id") "workflow/HelloWorld/processor/hello/iterationstrategy/"))
  (is (= (get strategy "@type") "IterationStrategyStack")))

(defn test-datalink [datalink]
  (is (not (nil? datalink)))
  (is (= (get datalink "@id") "workflow/HelloWorld/datalink?from=processor/hello/out/greeting&to=out/hello"))
  (is (= (get datalink "@type") "DataLink"))
  (is (= (get-in datalink ["receiveFrom" "@id"]) "workflow/HelloWorld/processor/hello/out/greeting"))
  (is (= (get-in datalink ["sendTo" "@id"]) "workflow/HelloWorld/out/hello")))

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

  (testing "workflow bundle uppercase uuid"
    (let [response (app (request :get "/workflowBundle/62EB2413-BFEC-4947-9854-CBABC7ECBC32/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json))))

  (testing "workflow bundle invalid uuid"
    (let [response (app (request :get "/workflowBundle/fred/"))]
      (is (= (:status response) 400))))
  
  (testing "workflow"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow")))))

  (testing "workflow invalid uuid"
    (let [response (app (request :get "/workflowBundle/fred/workflow/HelloWorld/"))]
      (is (= (:status response) 400))))

  (testing "processor"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/processor/hello/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-processor (get-in json ["workflow" "processor"])))))

  (testing "input processor port"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/processor/hello/in/name"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-processor (get-in json ["workflow" "processor"]))
        (test-input-processor-port (get-in json ["workflow" "processor" "inputProcessorPort"])))))

  (testing "output processor port"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/processor/hello/out/greeting"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-processor (get-in json ["workflow" "processor"]))
        (test-output-processor-port (get-in json ["workflow" "processor" "outputProcessorPort"])))))

  (testing "iteration strategy"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/processor/hello/iterationstrategy/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-processor (get-in json ["workflow" "processor"]))
        (test-iteration-strategy-stack (get-in json ["workflow" "processor" "iterationStrategyStack"])))))

  (testing "datalink"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/workflow/HelloWorld/datalink?from=processor/hello/out/greeting&to=out/hello"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (test-workflow-bundle json)
        (test-workflow (get json "workflow"))
        (test-datalink (get-in json ["workflow" "datalink"])))))
  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

