(ns main-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [main]))

(defn string->stream [s]
  (-> s
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)))

(deftest extract-lyrics-removes-newlines-test
  (testing "splits on newlines"
    (is (= '("a" "b") (sequence #'main/extract-lyrics [(string->stream "a\nb")])))))

(deftest extract-lyrics-removes-empty-strings-test
  (testing "removes empty strings"
    (is (= '("a" "b") (sequence #'main/extract-lyrics [(string->stream "a\n\n\n\nb")])))))
