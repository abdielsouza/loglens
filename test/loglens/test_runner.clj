(ns loglens.test-runner
  (:require [clojure.test :as test]
            [loglens.core-test]
            [loglens.parser-test]
            [loglens.analyzer-test]
            [loglens.reader-test]))

(defn -main [& _args]
  (let [result (test/run-tests
                'loglens.core-test
                'loglens.parser-test
                'loglens.analyzer-test
                'loglens.reader-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (System/exit 1))))