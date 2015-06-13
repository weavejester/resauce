(ns urldir.core
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

(defmulti dir
  "Return a list of resources under this URL, if possible."
  (fn [url] (.getScheme (URI. (str url)))))

(defmethod dir "file" [url]
  (if-let [path (.getPath (URI. (str url)))]
    (let [file (io/file path)]
      (if (.isDirectory file)
        (map io/as-url (.listFiles file))))))

(defmethod dir "jar" [url]
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
