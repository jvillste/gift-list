(ns gift-list.api
  (:require [shoreleave.middleware.rpc :as rpc ]
            (gift-list [data :as data]
                       [session :as session])))

(defmacro define-remote [function arguments]
  `(rpc/defremote ~(symbol (name function)) ~arguments
     (~function ~@arguments)))

(defmacro define-remotes [& specs]
  (let [specs (partition 2 specs)]
    `(do ~@(map (fn [[function arguments]]
                  `(define-remote ~function ~arguments))
                specs))))

(define-remotes
  data/gifts []
  data/gift [gift-id]
  data/reserve [gift-id]
  data/release [gfit-id]
  session/get-session-cookie [password])



