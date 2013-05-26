(ns gift-list.login
  (:require-macros [shoreleave.remotes.macros :as shoreleave])
  (:require [crate.core :as crate]
            [domina :as domina]
            [domina.events :as events]
            goog.net.cookies
            shoreleave.remotes.http_rpc))

(defn check-password [password]
  (shoreleave/rpc (get-session-cookie password)
                  [session-cookie]
                  (when session-cookie
                    (domina/set-text! (domina/by-id "message")
                                      "Oikein!")
                    (.set goog.net.cookies (:name session-cookie) (:value session-cookie) -1)
                    (set! (.-location js/window) "list"))))

(defn run []
  (shoreleave/rpc (question)
                  [question]
                  (domina/append! (.-body js/document) (crate/html [:div#contents
                                                                    [:img {:src "logo"}]
                                                                    [:div.question question]
                                                                    [:input#password {:type "text"}] [:span#message ""]]))
                  (let [password-editor (domina/by-id "password")]
                    (events/listen! password-editor
                                    :input  #(check-password (domina/value (domina/by-id "password")))))))