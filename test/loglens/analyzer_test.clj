
(ns loglens.analyzer-test
  (:require [clojure.test :refer [deftest is]]
            [loglens.analyzer :as analyzer]))

(def sample-events
  [{:level :info
    :message "Service started"
    :timestamp "2026-10-21T08:00:00Z"
    :parsed? true}
   {:level :error
    :message "Connection timeout"
    :timestamp "2026-10-21T08:01:00Z"
    :parsed? true}
   {:level :error
    :message "Connection timeout"
    :timestamp "2026-10-21T08:02:00Z"
    :parsed? true}
   {:level nil
    :message "Unknown log format"
    :timestamp nil
    :parsed? false}])

(deftest counts-events-and-levels
  (let [stats (analyzer/analyze-events sample-events)]
    (is (= 4 (:total stats)))
    (is (= 1 (get-in stats [:levels :info])))
    (is (= 2 (get-in stats [:levels :error]))
    )
    (is (nil? (get-in stats [:levels :unknown])))))

(deftest counts-unparsed-and-timestamped-events
  (let [stats (analyzer/analyze-events sample-events)]
    (is (= 1 (:unparsed stats)))
    (is (= 3 (:timestamped stats)))))

(deftest groups-repeated-messages
  (let [stats (analyzer/analyze-events sample-events)]
    (is (= 2 (get-in stats [:messages "Connection timeout"])))
    (is (= 1 (get-in stats [:messages "Service started"])))))

(deftest ranks-top-messages
  (let [stats (analyzer/analyze-events sample-events)]
    (is (= [{:message "Connection timeout" :count 2}
            {:message "Service started" :count 1}
            {:message "Unknown log format" :count 1}]
           (analyzer/top-messages stats 3)))))

(deftest supports-empty-input
  (is (= {:total 0
          :levels {}
          :messages {}
          :unparsed 0
          :timestamped 0}
         (analyzer/analyze-events []))))

(deftest does-not-mutate-previous-state
  (let [initial (analyzer/empty-stats)
        event {:level :error
               :message "Failure"
               :timestamp nil
               :parsed? true}
        updated (analyzer/update-stats initial event)]
    (is (= 0 (:total initial)))
    (is (= 1 (:total updated)))))
