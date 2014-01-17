(ns cloj-mop.core
  (:gen-class)
  (:import (com.mongodb MongoClient ReadPreference Bytes BasicDBObject))
  (:import (org.bson.types ObjectId))
  (:use [clojure.string :only (split triml)]))


; TODO: find to check the cursor is working ok
(defn get-oplog [client]
  (.. (.getDB client "local")
      (getCollection "oplog.rs")))

; TODO: timestamp param
(defn setup-oplog-cursor [cursor]
  (.. (.find cursor)
      (addOption Bytes/QUERYOPTION_TAILABLE)
      (addOption Bytes/QUERYOPTION_AWAITDATA)))

(defn should-process [database expected-db-name collection dbo]
  (and (= database expected-db-name)
       (not= (.startsWith collection "system")
       (not (.get dbo "fromMigrate")))))

(defn parse-namespace [ns]
  (split ns #"\." 2))

(defn get-document [client database collection doc-id]
  (.. (.getDB client database)
      (getCollection collection)
      (findOne (BasicDBObject. "_id" (if (ObjectId/isValid doc-id) (ObjectId. doc-id) doc-id)))))

(defrecord Record [collection id operation timestamp])

(defn loopit [cursor]
  (let [rec (.next cursor) nsp (parse-namespace (.get rec "ns"))]
    rec))

(defn mong []
  (let [client (MongoClient.)]
    (.setReadPreference client ReadPreference/PRIMARY)
    (println (format "mongodb client version: %d.%d"
                     (MongoClient/getMajorVersion)
                     (MongoClient/getMinorVersion)))
    (println (format "mongodb server version: %s"
                     (.. (.getDB client "admin")
                         (command "serverStatus")
                         (get "version"))))
    (let [cursor (setup-oplog-cursor (get-oplog client))]
      (println (format "reading oplog from mongo instance %s "
                       (.. (.explain cursor) (get "server"))))
      cursor)))
      ; (while (.hasNext cursor)
      ;   (let [rec (.next cursor) nsp (parse-namespace (.get rec "ns"))]
      ;     (if (should-process (first nsp) "leedb" (second nsp) rec)
      ;       (if (= (.get rec "op") "i")
      ;         ;(println (format "entry %s" rec)
      ;         ;(println (format "entry record %s" (Record. "a" "b" "c" "d")))))))))))
      ;         "bar")))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [cursor (mong)]
    (dorun (repeatedly #(println (format "rec: %s" (loopit cursor)))))))
