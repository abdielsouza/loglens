
(ns loglens.parser
  (:require [clojure.string :as str]))

(def ^:private timestamp-patterns
  [#"^\[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2}))\]\s*(.*)$"
   #"^(\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}(?:[.,]\d+)?)\s+(.*)$"])

(def ^:private level-pattern
  #"(?i)(?:^|[\s\[\]])(TRACE|DEBUG|INFO|WARN(?:ING)?|ERROR|FATAL)(?=$|[\s:\]\[])")

(def ^:private source-pattern
  #"\[([^\]]+)\]")

(defn- parse-timestamp [line]
  (some
   (fn [pattern]
     (when-let [[_ timestamp rest] (re-matches pattern line)]
       {:timestamp timestamp
        :rest rest}))
   timestamp-patterns))

(defn- parse-level [text]
  (when-let [[_ level] (re-find level-pattern text)]
    (case (str/upper-case level)
      "TRACE" :trace
      "DEBUG" :debug
      "INFO" :info
      "WARN" :warn
      "WARNING" :warn
      "ERROR" :error
      "FATAL" :fatal)))

(defn- parse-source [text]
  (second (re-find source-pattern text)))

(defn- remove-level [text]
  (str/replace-first text level-pattern " "))

(defn- clean-message [text]
  (-> text
      (str/replace source-pattern " ")
      (str/replace #"^[\s:|\-]+" "")
      str/trim))

(defn parse-line
  "Converte uma linha de log em um evento normalizado."
  [line]
  (let [raw (str line)
        timestamp-result (parse-timestamp raw)
        timestamp (:timestamp timestamp-result)
        body (or (:rest timestamp-result) raw)
        level (parse-level body)
        source (parse-source body)
        message (clean-message (remove-level body))]
    {:raw raw
     :timestamp timestamp
     :level level
     :source source
     :message message
     :parsed? (boolean (or timestamp level source))}))
