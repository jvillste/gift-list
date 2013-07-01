(ns gift-list.data
  (:require [monger.core :as monger]
            [monger.collection :as collection]
            [monger.operators :as operators]
            [shoreleave.middleware.rpc :as rpc ]
            [clojure.java.io :as io]))

(def gifts-collection "gifts")
(def settings-collection "settings")
(def reservations-collection "reservations")

(defn connect-to-db
  ([uri]
     (println "connecting to" uri)
     (monger/connect-via-uri! uri ))
  ([]
     (monger/connect!)
     (monger/set-db! (monger/get-db "gift-list"))))

(defn disconnect []
  (monger/disconnect!))

(defn read-file [file-name]
  (let [output (byte-array (.length (io/as-file file-name)))]
    (with-open [input (io/input-stream file-name)]
      (.read input output))
    output))


(defn get-setting [key]
  (key (first (collection/find-maps settings-collection ))))

(defn reserved [id]
  (if-let [reservation (collection/find-map-by-id reservations-collection id)]
    (:reserved reservation)
    nil))

(defn gift-to-dto [gift]
  (-> gift
      (assoc :id (:_id gift))
      (dissoc :_id)
      (assoc :reserved (reserved (:_id gift)))))

(defn question []
  (get-setting :question))

(defn logo []
  (get-setting :logo))

(defn gifts []
  (->> (collection/find-maps gifts-collection)
       (map gift-to-dto)))

(defn gift [id]
  (if-let [gift (collection/find-map-by-id gifts-collection id)]
    (gift-to-dto gift)
    nil))

(defn reserve [gift-id]
  (let [gift (gift gift-id)]
    (when (< (:reserved gift)
             (:max gift))
      (collection/update reservations-collection gift-id {operators/$inc {:reserved 1}}))
    nil))

(defn release [gift-id]
  (let [gift (gift gift-id)]
    (when (> (:reserved gift)
             0)
      (collection/update reservations-collection gift-id {operators/$inc {:reserved -1}})))
  nil)

(defn reset [data]
  (collection/remove gifts-collection)
  (let [gifts (map-indexed (fn [index gift]
                             (if (not (reserved (:_id gift)))
                               (collection/insert reservations-collection {:_id (:_id gift) :reserved 0}))
                             (assoc gift :index index))
                           (:gifts data))]
    (collection/insert-batch gifts-collection gifts))



  (collection/remove settings-collection)
  (collection/insert settings-collection (:settings data)))

(defn reset-reservations []
  (collection/remove reservations-collection)
  (doseq [gift (gifts)]
    (collection/insert reservations-collection {:_id (:id gift) :reserved 0})))

(def defaults {:gifts [{:_id 1
                        :description (apply str (repeat 50 "bla "))
                        :max 1}
                       {:_id 2
                        :description (apply str (repeat 130 "bla "))
                        :max 3}]
               :settings {:logo "images/logo.jpg"
                          :password "foo"
                          :question "Give me foo"
                          :session-key "123jlk12j3"}})
