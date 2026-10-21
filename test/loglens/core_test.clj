(ns loglens.core-test
  (:require [clojure.test :refer [deftest is]]
            [loglens.core :as core]))

(deftest app-info-test
  (is (= "LogLens" (:name core/app-info)))
  (is (= "0.1.0" (:version core/app-info))))