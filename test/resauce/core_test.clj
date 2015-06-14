(ns resauce.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [resauce.core :refer :all]))

(deftest test-directory?
  (is (directory? (io/resource "resauce")))
  (is (directory? (io/resource "clojure")))
  (is (not (directory? (io/resource "resauce/core.clj"))))
  (is (not (directory? (io/resource "clojure/core.clj")))))
