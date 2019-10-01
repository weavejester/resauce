(defproject resauce "0.1.0"
  :description "Useful functions for handling JVM resources"
  :url "https://github.com/weavejester/resauce"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.jboss/jboss-vfs "3.2.14.Final"]]
  :plugins [[codox "0.8.11"]]
  :profiles {:test {:dependencies [[medley "0.6.0"]]}})
