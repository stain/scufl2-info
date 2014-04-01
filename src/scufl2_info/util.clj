(ns scufl2-info.util)

(defn normalize-uuid [uuid]
  (str (java.util.UUID/fromString uuid)))  

(defn uuid-test [uuid]
  "Check the uuid is valid. If the uuid is invalid, a ring response map
  with a 400 error message is returned, otherwise nil is returned.
  
  Example use:

  (GET \"test/:uuid/\" (or (uuid-test uuid) (str \"Your uuid is \" uuid)))
  "
   (try 
     (java.util.UUID/fromString uuid)
     nil
    (catch Exception e
      { :status 400 :body (str "Invalid UUID: " uuid) })))

