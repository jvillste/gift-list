(ns gift-list.session
  (:require [gift-list.data :as data]))

(def session-cookie-name "gift-list")

(defn password-valid? [password]
  (= (.toUpperCase password)
     (.toUpperCase (data/get-setting :password))))

(defn get-session-cookie [password]
  (if (password-valid? password)
    {:name session-cookie-name
     :value (data/get-setting :session-key)} 
    nil))

(defn session-open? [cookies]
  (= (:value (cookies session-cookie-name))
     (data/get-setting :session-key)))

