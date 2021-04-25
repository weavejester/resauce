(ns resauce.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string]
            [resauce.core :refer :all]
            ))

(deftest test-directory?
  (is (directory? (io/resource "resauce")))
  (is (directory? (io/resource "clojure")))
  (is (not (directory? (io/resource "resauce/core.clj"))))
  (is (not (directory? (io/resource "clojure/core.clj"))))
  (is (not (directory? nil))))

(deftest test-resources
  (let [rs (sort (map str (resources "resauce")))]
    (is (= (count rs) 2))
    (is (re-find #"src/resauce$" (first rs)))
    (is (re-find #"test/resauce$" (second rs)))))

(deftest test-url-dir
  (testing "file URL"
    (let [rs (url-dir (io/as-url (io/file "src/resauce")))]
      (is (= (count rs) 1))
      (is (re-matches #"file:.*src/resauce/core\.clj" (str (first rs))))))
  (testing "jar URL"
    (let [rs (sort (map str (url-dir (io/resource "medley"))))]
      (is (= (count rs) 3))
      (is (re-matches #"jar:.*medley/core\.clj" (first rs)))
      (is (re-matches #"jar:.*medley/core\.cljs" (second rs)))
      (is (re-matches #"jar:.*medley/core\.cljx" (nth rs 2))))))

(deftest test-resource-dir
  (let [rs (sort (map str (resource-dir "resauce")))]
    (is (= (count rs) 2))
    (is (re-find #"src/resauce/core\.clj$" (first rs)))
    (is (re-find #"test/resauce/core_test\.clj$" (second rs)))))

(deftest test-resource-name
  (let [names-in-file (sort (resource-dir-names "resauce"))
        names-in-jar (sort (resource-dir-names "medley"))
        names-in-tree (sort (resource-dir-names-tree "hiccup"))]
    ; resauce - file
    (is (= (first names-in-file) "core.clj"))
    (is (= (second names-in-file) "core_test.clj"))
    ; medley - jar
    (is (= names-in-jar
           ["core.clj" 
            "core.cljs"
            "core.cljx"]))
    ; medley - directory tree
    (is (= names-in-tree
           ["hiccup/compiler.clj" 
            "hiccup/core.clj"
            "hiccup/def.clj" 
            "hiccup/element.clj" 
            "hiccup/form.clj" 
            "hiccup/gamma.clj" ; file (src-test/hiccup)
            "hiccup/middleware.clj" 
            "hiccup/page.clj" 
            "hiccup/util.clj" 
            "hiccup/zeta/core.clj" ; file (src-test/hiccup)
            ]))))