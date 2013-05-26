(ns gift-list.list
  (:require-macros [shoreleave.remotes.macros :as shoreleave])
  (:require [crate.core :as crate]
            [domina :as domina]
            [domina.events :as events]
            shoreleave.remotes.http_rpc))

(defn widget [renderer & params]
  (let [placeholder (crate/html [:div])]
    (apply renderer (conj params placeholder))
    placeholder))


(defn button [label handler]
  (let [button (crate/html [:a.button.active-button {:href "javascript:void(0)"} label])]
    (events/listen! button
                    :click handler)
    button))

(defn disabled-button [label]
  (crate/html [:a.button.disabled-button {:href "javascript:void(0)"} label]))

(defn colored-text [color text]
  [:span {:style (str "color:" color)} text])

(defn render-gift [parent gift-id message]
  (shoreleave/rpc (gift gift-id)
                  [{:keys [reserved max description]}]
                  (domina/destroy-children! parent)
                  (domina/append! parent (crate/html [:div.gift
                                                      (crate/raw description)
                                                      [:div.spacer]
                                                      [:div.reservation
                                                       [:div.status  "varattu: " reserved " " (let [free (- max reserved)
                                                                                                      color (if (= 0 free)
                                                                                                              "red"
                                                                                                              "black")]
                                                                                                  (colored-text color (str "vapaana: " free)))]

                                                       (if (> reserved 0)
                                                         (button "Peru varaus" #(shoreleave/rpc (release gift-id)
                                                                                                [_]
                                                                                                (render-gift parent gift-id (colored-text "green" "Varaus peruttu." ))))
                                                         (disabled-button "Peru varaus"))
                                                       (if (< reserved max)
                                                         (button "Tee varaus" #(shoreleave/rpc (reserve gift-id) [success]
                                                                                               (if success
                                                                                                 (render-gift parent gift-id (colored-text "green" "Kiitos varauksesta!" ))
                                                                                                 (render-gift parent gift-id (colored-text "red" "Joku muu ehti juuri varata tämän lahjan ennen sinua! Varauksesi ei siis onnistunut.")))))
                                                         (disabled-button "Tee varaus"))]
                                                      [:div.message message]]))))


(defn gift-widget [gift-id]
  (widget render-gift gift-id ""))

(defn show-list [gifts]
  (domina/append! (.-body js/document) (crate/html [:div#contents
                                                    [:img {:src "logo"}]
                                                    [:div#gift-list]] ))
  (let [gift-list (domina/by-id "gift-list")]
    (doseq [gift gifts]
      (domina/append! gift-list (gift-widget (:id gift))))))

(defn fetch-list []
  (shoreleave/rpc (gifts)
                  [gifts]
                  (show-list gifts)))

(defn run []
  (fetch-list))
