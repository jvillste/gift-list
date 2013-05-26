(ns gift-list.initialize
  (:require [gift-list.data :as data]))

(defn -main
  ([uri file-name]
     (data/connect-to-db uri)
     (data/reset (read-string (slurp file-name))))
  ([uri]
     (data/connect-to-db uri)
     (data/reset data/defaults)))