(defproject resauce "0.2.0"
  :description "Useful functions for handling JVM resources"
  :url "https://github.com/weavejester/resauce"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-codox "0.10.7"]]
  :codox {:output-path "codox"}
  :profiles {:test { :source-paths ["src-test"]
                    :dependencies [[medley "0.6.0"]
                                   [hiccup "1.0.5"]
                                   ]}})
