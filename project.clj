(defproject cloj-mop "0.1.0-SNAPSHOT"
  :description "FIXME: mongodb oplog tailer written in clojure"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"],[org.mongodb/mongo-java-driver "2.11.3"]]
  :main ^:skip-aot cloj-mop.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
