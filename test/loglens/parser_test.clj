
(ns loglens.parser-test
  (:require [clojure.test :refer [deftest is]]
            [loglens.parser :as parser]))

(deftest parses-timestamp-level-source-and-message
  (let [event (parser/parse-line
               "2026-10-21 08:35:12 ERROR [worker-1] Connection timeout")]
    (is (= "2026-10-21 08:35:12" (:timestamp event)))
    (is (= :error (:level event)))
    (is (= "worker-1" (:source event)))
    (is (= "Connection timeout" (:message event)))
    (is (true? (:parsed? event)))))

(deftest parses-bracketed-iso-timestamp
  (let [event (parser/parse-line
               "[2026-10-21T08:35:13Z] WARN Database connection is slow")]
    (is (= "2026-10-21T08:35:13Z" (:timestamp event)))
    (is (= :warn (:level event)))
    (is (= "Database connection is slow" (:message event)))))

(deftest parses-level-without-timestamp
  (let [event (parser/parse-line "ERROR: Could not load configuration")]
    (is (nil? (:timestamp event)))
    (is (= :error (:level event)))
    (is (= "Could not load configuration" (:message event)))
    (is (true? (:parsed? event)))))

(deftest preserves-unknown-lines
  (let [line "Falha inesperada ao processar a solicitação"
        event (parser/parse-line line)]
    (is (= line (:raw event)))
    (is (= line (:message event)))
    (is (nil? (:level event)))
    (is (false? (:parsed? event)))))

(deftest normalizes-warning-level
  (is (= :warn
         (:level (parser/parse-line "WARNING: Slow response")))))

(deftest handles-empty-lines
  (let [event (parser/parse-line "")]
    (is (= "" (:raw event)))
    (is (= "" (:message event)))
    (is (false? (:parsed? event)))))
