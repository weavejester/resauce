(ns resauce.core
  (:require [clojure.java.io :as io])
  (:import [java.net JarURLConnection URI URL]
           [java.util.regex Pattern]))

(defn- add-ending-slash [^String s]
  (if (.endsWith s "/") s (str s "/")))

(defn- filter-dir-paths [dir paths]
  (let [re (re-pattern (str (Pattern/quote (add-ending-slash dir)) "[^/]+/?"))]
    (filter (partial re-matches re) paths)))

(defn- build-url [base-url dir path]
  (if (.startsWith ^String path dir)
    (str (add-ending-slash (str base-url)) (subs path (count dir)))))

(defmulti url-dir
  "Return a list of URLs contained by this URL, if the protocol supports it."
  (fn [url] (.getScheme (URI. (str url)))))

(defmethod url-dir "file" [url]
  (if-let [path (.getPath (URI. (str url)))]
    (let [file (io/file path)]
      (if (.isDirectory file)
        (map io/as-url (.listFiles file))))))

(defmethod url-dir "jar" [url]
  (let [conn  (.openConnection (URL. (str url)))
        jar   (.getJarFile ^JarURLConnection conn)
        path  (add-ending-slash (.getEntryName ^JarURLConnection conn))
        entry (.getEntry jar path)]
    (if (and entry (.isDirectory entry))
      (->> (.entries jar)
           (iterator-seq)
           (map (memfn getName))
           (filter-dir-paths path)
           (map (partial build-url url path))))))

(defn resources
  "Returns *all* the URLs for a named resource. Uses the context class loader
  if no loader is specified."
  ([n] (resources n (.getContextClassLoader (Thread/currentThread))))
  ([n ^ClassLoader loader] (enumeration-seq (.getResources loader n))))

(defn resource-dir
  "Return a list of resource URLs on the classpath that have the supplied
  path prefix."
  [path]
  (mapcat url-dir (resources path)))
