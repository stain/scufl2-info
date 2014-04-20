(ns scufl2-info.test.jsonld
  (:use clojure.test
        scufl2-info.jsonld))

(deftest test-jsonld
  (testing "jsonld-to-turtle"
    (let [jsonld { "@id"   "http://example.com/a"
                   "@type" "http://example.org/b" 
                   "http://example.net/c" "d" }
          turtle (jsonld-to-turtle jsonld)]
      (is (. turtle contains "<http://example.com/a>"))
      (is (. turtle contains " a "))
      (is (. turtle contains "<http://example.org/b>"))
      (is (. turtle contains "<http://example.net/c>"))
      (is (. turtle contains "\"d\"")))))


              


