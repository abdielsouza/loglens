
(ns loglens.core
  (:require [loglens.reader :as reader]
            [loglens.reporter :as reporter])
  (:gen-class))

(def app-info
  {:name "LogLens"
   :version "0.1.0"})

(defn run-cli [args]
  (let [[command path & options] args
        opts (when (even? (count options))
               (apply hash-map options))
        format (get opts "--format" "text")
        output-path (get opts "--output")]
    (cond
      (not= command "analyze")
      (println "Uso: clojure -M:run analyze <arquivo.log> [--format text|json] [--output arquivo]")
  
      (nil? path)
      (binding [*out* *err*]
        (println "LogLens: informe o caminho do arquivo."))
  
      (nil? opts)
      (binding [*out* *err*]
        (println "LogLens: opções devem ser informadas em pares."))
  
      (not (#{"text" "json"} format))
      (binding [*out* *err*]
        (println "LogLens: formato inválido. Use text ou json."))
  
      :else
      (try
        (let [stats (reader/analyze-file path)
              report (case format
                       "json" (reporter/render-json stats)
                       "text" (reporter/render-text stats))]
          (if output-path
            (spit output-path (str report "\n"))
            (println report)))
        (catch java.io.IOException e
          (binding [*out* *err*]
            (println "LogLens: erro de arquivo:" (.getMessage e)))
          (System/exit 2))))))

(defn -main
  [& args]
  (let [status (run-cli args)]
    (when (pos? status)
      (System/exit status))))