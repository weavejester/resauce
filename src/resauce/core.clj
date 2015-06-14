(ns resauce.core
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [java.net JarURLConnection URI URL]
           [java.util.regex Pattern]))

(defn- add-ending-slash [^String s]
  (if (.endsWith s "/") s (str s "/")))

(defn- filter-dir-paths [dir paths]
  (let [re (re-pattern (str (Pattern/quote (add-ending-slash dir)) "[^/]+/?"))]
    (filter (partial re-matches re) paths)))

(defn- build-url [base-url dir path]
  (if (.startsWith ^String path dir)
    (str (add-ending-slash (str base-url))
         (subs path (count (add-ending-slash dir))))))

(defn- url-scheme [url]
  (.getScheme (URI. (str url))))

(defn- url-file [url]
  (File. (.getPath (URI. (str url)))))

(defmulti directory?
  "Return true if a URL points to a directory resource."
  {:arglists '([url])}
  url-scheme)

(defmethod directory? "file" [url]
  (let [file (url-file url)]
    (and (.exists file) (.isDirectory file))))

(defmethod directory? "jar" [url]
  (let [conn  (.openConnection (URL. (str url)))
        jar   (.getJarFile ^JarURLConnection conn)
        path  (.getEntryName ^JarURLConnection conn)
        entry (.getEntry jar (add-ending-slash path))]
    (and entry (.isDirectory entry))))

(defmulti url-dir
  "Return a list of URLs contained by this URL, if the protocol supports it."
  {:arglists '([url])}
  url-scheme)

(defmethod url-dir "file" [url]
  (map io/as-url (.listFiles (url-file url))))

(defmethod url-dir "jar" [url]
  (let [conn (.openConnection (URL. (str url)))
        jar  (.getJarFile ^JarURLConnection conn)
        path (.getEntryName ^JarURLConnection conn)]
    (->> (.entries jar)
         (iterator-seq)
         (map (memfn getName))
         (filter-dir-paths path)
         (map (partial build-url url path)))))

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
