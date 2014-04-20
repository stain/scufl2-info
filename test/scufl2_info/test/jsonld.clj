(ns scufl2-info.test.jsonld
  (:use clojure.test
        scufl2-info.jsonld))

(defn verify-turtle [turtle]
  ;(print turtle)
      (is (. turtle contains "<http://example.com/a>"))
  ;; Not true for ntriples
      ;(is (. turtle contains " a "))
      (is (. turtle contains "<http://example.org/b>"))
      (is (. turtle contains "<http://example.net/c>"))
      (is (. turtle contains "\"d\"")))

(defn verify-rdfxml [rdfxml]
      (is (. rdfxml contains "\"http://example.com/a\""))
      (is (. rdfxml contains "rdf:about"))
      (is (. rdfxml contains "http://example.org/"))
      (is (. rdfxml contains "http://example.net/"))
      (is (. rdfxml contains ">d<")))

(deftest test-jsonld
  (testing "jsonld-to-turtle"
    (let [jsonld { "@id"   "http://example.com/a"
                   "@type" "http://example.org/b" 
                   "http://example.net/c" "d" }
          turtle (jsonld-to-rdf jsonld :turtle)
          ntriples (jsonld-to-rdf jsonld :ntriples)
          rdfxml (jsonld-to-rdf jsonld :rdfxml)
          ]
      (verify-turtle turtle)
      (verify-turtle ntriples)
      (verify-rdfxml rdfxml)
      )))


              


