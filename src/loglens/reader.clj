
(ns loglens.reader
  (:require [clojure.java.io :as io]
            [loglens.parser :as parser]
            [loglens.analyzer :as analyzer]))

(defn analyze-reader
  "Analisa linhas de um Reader, uma por vez."
  [reader]
  (with-open [r (io/reader reader)]
    (reduce
     (fn [stats line]
       (analyzer/update-stats stats (parser/parse-line line)))
     (analyzer/empty-stats)
     (line-seq r))))

(defn analyze-file
  "Analisa um arquivo de log pelo caminho informado."
  [path]
  (analyze-reader (io/file path)))
