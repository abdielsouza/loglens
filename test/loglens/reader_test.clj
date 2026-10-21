
(ns loglens.reader-test
  (:require [clojure.test :refer [deftest is]] 
            [loglens.reader :as reader]))

(deftest analyzes-sample-file
  (let [stats (reader/analyze-file "samples/application.log")]
    (is (= 7 (:total stats)))
    (is (= 2 (get-in stats [:levels :info])))
    (is (= 3 (get-in stats [:levels :error])))
    (is (= 1 (get-in stats [:levels :warn])))
    (is (= 1 (:unparsed stats)))
    (is (= 5 (:timestamped stats)))
    (is (= 2 (get-in stats [:messages "Connection timeout"])))))

(deftest analyzes-reader
  (let [stats (reader/analyze-reader
               (java.io.StringReader.
                "INFO: Started\nERROR: Failed\n"))]
    (is (= 2 (:total stats)))
    (is (= 2 (get-in stats [:levels :info])))
    (is (= 3 (get-in stats [:levels :error])))
    (is (= 1 (get-in stats [:levels :warn])))
    (is (= 5 (:timestamped stats)))))

(deftest analyzes-empty-file
  (let [file (java.io.File/createTempFile "loglens-" ".log")]
    (try
      (spit file "")
      (is (= 0 (:total (reader/analyze-file (.getPath file)))))
      (finally
        (.delete file)))))
