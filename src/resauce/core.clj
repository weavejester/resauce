(ns resauce.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]
           [java.net JarURLConnection URI URL]
           [java.util.jar JarEntry]
           [java.util.regex Pattern]))

(defn- add-ending-slash [^String s]
  (if (.endsWith s "/") s (str s "/")))

(defn- filter-dir-paths [dir paths]
  (let [re (re-pattern (str (Pattern/quote (add-ending-slash dir)) "[^/]+/?"))]
    (filter (partial re-matches re) paths)))

(defn- build-url [base-url dir path]
  {:pre [(.startsWith ^String path dir)]}
  (URL. (str (add-ending-slash (str base-url))
             (subs path (count (add-ending-slash dir))))))

(defn- url-scheme [url]
  ;; Using URI instead of URL to support arguments without schema.
  (.getScheme (URI. (str url))))

(defn- ^File url-file [url]
  (File. ^String (.getPath (io/as-url url))))

(defmulti directory?
  "Return true if a URL points to a directory resource."
  {:arglists '([url])}
  url-scheme)

(defmethod directory? "file" [url]
  (let [file (url-file url)]
    (and (.exists file) (.isDirectory file))))

(defmethod directory? "jar" [url]
  (let [conn  (.openConnection (io/as-url url))
        jar   (.getJarFile ^JarURLConnection conn)
        path  (.getEntryName ^JarURLConnection conn)
        entry (.getEntry jar (add-ending-slash path))]
    (and entry (.isDirectory entry))))

(defmethod directory? :default [url]
  false)

(defmulti url-dir
  "Return a list of URLs contained by this URL, if the protocol supports it."
  {:arglists '([url])}
  url-scheme)

(defmethod url-dir "file" [url]
  (map io/as-url (.listFiles (url-file url))))

(defmethod url-dir "jar" [url]
  (let [conn (.openConnection (io/as-url url))
        jar  (.getJarFile ^JarURLConnection conn)
        path (.getEntryName ^JarURLConnection conn)]
    (->> (.entries jar)
         (enumeration-seq)
         (map (memfn ^JarEntry getName))
         (filter-dir-paths path)
         (map (partial build-url url path)))))



(defmulti res-name
  {:arglists '([url])}
  url-scheme)

(defmethod res-name "file" [url]
  (let [file (io/as-file url)]
    ;(.getPath file)
    (.getName file)))

(defmethod res-name "jar" [url]
  (let [conn  (.openConnection (io/as-url url))
        jar   (.getJarFile ^JarURLConnection conn)
        path  (.getEntryName ^JarURLConnection conn)
        entry (.getEntry jar path)
        name (.getName entry)]
    ;path
    ;(println "path: " path " entry: " entry  " name: " (.getName entry))
    (last (str/split name #"/"))
    ;(.getTime entry)
    ))

(defmethod res-name :default [url]
  nil)

(defn- default-loader []
  (.getContextClassLoader (Thread/currentThread)))

(defn resources
  "Returns *all* the URLs for a named resource. Uses the context class loader
  if no loader is specified."
  ([n] (resources n (default-loader)))
  ([n ^ClassLoader loader] (enumeration-seq (.getResources loader n))))

(defn resource-dir
  "Return a list of resource URLs on the classpath that have the supplied
  path prefix."
  ([path] (resource-dir path (default-loader)))
  ([path loader] (mapcat url-dir (resources path loader))))




(defn resource-dir-names
  ([path]
   (resource-dir-names path (default-loader)))
  ([path loader]
   (map res-name (->> (resource-dir path loader)
                      (remove directory?)
                      (into [])
                      ))))

(defn- join-path [base f]
  (str base "/" f))

(defn excluded? [name]
  (= "META-INF" name))

(defn resource-dir-names-tree
  ([path]
   (resource-dir-names-tree [] path (default-loader)))
  ([path loader]
   (resource-dir-names-tree [] path loader))
  ([acc path loader]
   (let [urls (resource-dir path loader)]
     ;(println "* resource path: " path " item count: " (count urls) urls)
     (reduce (fn [names url]
               (let [name (res-name url)
                     name-with-path (if (str/blank? path)
                                      name
                                      (str path "/" name))]
                 (if (excluded? name)
                   (do ;(println "excluded: " name)
                       names)
                   (if (directory? url)
                     (do ;(println "recursing into dir: " name-with-path " url: " url)
                         (concat names (resource-dir-names-tree names name-with-path loader)))
                     (conj names name-with-path)))))
             acc
             urls))))
