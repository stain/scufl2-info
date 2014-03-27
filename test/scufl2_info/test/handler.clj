(ns scufl2-info.test.handler
  (:use clojure.test
        ring.mock.request  
        cheshire.core 
        scufl2-info.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "workflow bundle"
    (let [response (app (request :get "/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/"))]
      (is (= (:status response) 200))
      (let [json (parse-string (:body response))]
        (is (= (get json "@id") "http://ns.taverna.org.uk/2010/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/" ))
        (is (= (get json "@type") "WorkflowBundle"))
        (is (contains? json "@context"))
        (is (= (get-in json ["@context" "@vocab"]) "http://ns.taverna.org.uk/2010/scufl2#"))
        (is (= (get-in json ["@context" "@base"]) "http://ns.taverna.org.uk/2010/workflowBundle/62eb2413-bfec-4947-9854-cbabc7ecbc32/")))))

  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
