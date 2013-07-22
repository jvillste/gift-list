(ns gift-list.login
  (:require shoreleave.remotes.http-rpc
            [crate.core :as crate]
            [domina :as domina]
            [domina.events :as events]
            goog.net.cookies)
  (:require-macros [shoreleave.remotes.macros :as shoreleave]))


(defn check-password [password]
  (shoreleave/rpc (get-session-cookie password)
                  [session-cookie]
                  (when session-cookie
                    (domina/set-text! (domina/by-id "message")
                                      "Oikein!")
                    (.set goog.net.cookies (:name session-cookie) (:value session-cookie) -1)
                    (set! (.-location js/window) "list"))))



(defn ^:export run [model]
  (let [model (js->clj model :keywordize-keys true)]
    (domina/append! (.-body js/document) (crate/html [:div#contents
                                                      [:img {:src (:logo model)}]
                                                      (if (:closing_message model)
                                                        [:div.question (:closing_message model)]
                                                        [:div
                                                         [:span.question (:question model)]
                                                         [:input#password {:type "text"}] [:span#message.green-text ""]])]))

    (when (not (:closing_message model))
      (let [password-editor (domina/by-id "password")]
        (events/listen! password-editor
                        :keyup #(check-password (domina/value (domina/by-id "password"))))))))
