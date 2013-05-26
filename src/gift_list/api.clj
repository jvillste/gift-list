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
  data/question []
  data/gifts []
  data/gift [gift-id]
  data/reserve [gift-id]
  data/release [gfit-id]
  session/get-session-cookie [password])

(comment

  (rpc/defremote question []
    (data/question))

  (rpc/defremote gifts []
    (data/gifts))

  (rpc/defremote gift [gift-id]
    (data/gift gift-id))

  (rpc/defremote reserve [gift-id]
    (data/reserve gift-id))

  (rpc/defremote release [gift-id]
    (data/release gift-id))

  (rpc/defremote get-session-cookie [password]
    (session/get-session-cookie password)))