(ns urldir.core
  (:require [clojure.java.io :as io])
  (:import [java.net URI]))

(defmulti dir
  "Return a list of resources under this URL, if possible."
  (fn [url] (.getScheme (URI. (str url)))))

(defmethod dir "file" [url]
  (if-let [path (.getPath (URI. (str url)))]
    (let [file (io/file path)]
      (if (.isDirectory file)
        (map io/as-url (.listFiles file))))))
