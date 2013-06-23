(ns gift-list.initialize
  (:require [gift-list.data :as data]))

(defn -main
  ([file-name]
     (data/connect-to-db)
     (data/reset (eval (read-string (slurp file-name)))))
  ([]
     (data/connect-to-db)
     (data/reset data/defaults)))
