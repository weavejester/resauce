(ns resauce.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [resauce.core :refer :all]))

(deftest test-directory?
  (is (directory? (io/resource "resauce")))
  (is (directory? (io/resource "clojure")))
  (is (not (directory? (io/resource "resauce/core.clj"))))
  (is (not (directory? (io/resource "clojure/core.clj")))))

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
