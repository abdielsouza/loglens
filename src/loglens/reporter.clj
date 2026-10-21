
(ns loglens.reporter
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [loglens.analyzer :as analyzer]))

(defn render-text
  "Produz um resumo legível das estatísticas."
  [stats]
  (let [levels [:fatal :error :warn :info :debug :trace]
        level-lines
        (for [level levels
              :let [count (get-in stats [:levels level] 0)]
              :when (pos? count)]
          (format "  %-7s %d"
                  (str/upper-case (name level))
                  count))
        message-lines
        (for [{:keys [message count]}
              (analyzer/top-messages stats 5)]
          (format "  %5d  %s" count message))]
    (str
     "LogLens — relatório de análise\n"
     "==============================\n"
     (format "Eventos processados: %d\n" (:total stats))
     (format "Com timestamp:       %d\n" (:timestamped stats))
     (format "Não interpretados:   %d\n" (:unparsed stats))
     "\nEventos por nível:\n"
     (if (seq level-lines)
       (str (str/join "\n" level-lines) "\n")
       "  Nenhum nível identificado\n")
     "\nMensagens mais frequentes:\n"
     (if (seq message-lines)
       (str (str/join "\n" message-lines) "\n")
       "  Nenhuma mensagem\n"))))


(defn render-json [stats]
  (json/generate-string
   {:total (:total stats)
    :timestamped (:timestamped stats)
    :unparsed (:unparsed stats)
    :levels
    (into {}
          (map (fn [[level count]]
                 [(name level) count]))
          (:levels stats))
    :top-messages
    (mapv (fn [{:keys [message count]}]
            {:message message
             :count count})
          (analyzer/top-messages stats 5))}
   {:pretty true}))
