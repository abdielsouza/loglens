
(ns loglens.analyzer)

(defn empty-stats
  "Cria o estado inicial das estatísticas."
  []
  {:total 0
   :levels {}
   :messages {}
   :unparsed 0
   :timestamped 0})

(defn update-stats
  "Incorpora um evento ao estado estatístico."
  [stats event]
  (cond-> (update stats :total inc)

    (:level event)
    (update-in [:levels (:level event)] (fnil inc 0))

    (some? (:message event))
    (update-in [:messages (:message event)] (fnil inc 0))

    (not (:parsed? event))
    (update :unparsed inc)

    (:timestamp event)
    (update :timestamped inc)))

(defn analyze-events
  "Calcula estatísticas a partir de uma coleção de eventos."
  [events]
  (reduce update-stats (empty-stats) events))

(defn top-messages
  "Retorna as mensagens mais frequentes, em ordem decrescente."
  ([stats]
   (top-messages stats 10))
  ([stats limit]
   (->> (:messages stats)
        (sort-by (fn [[message count]]
                   [(- count) message]))
        (take (max 0 limit))
        (mapv (fn [[message count]]
                {:message message
                 :count count})))))
