(ns gift-list.data
  (:require [monger.core :as monger]
            [monger.collection :as collection]
            [monger.operators :as operators]
            [shoreleave.middleware.rpc :as rpc ]
            [clojure.java.io :as io])
  (:import org.bson.types.ObjectId))

(def gifts-collection "gifts")
(def settings-collection "settings")

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

(defn reset [data]
  (collection/remove gifts-collection)
  (collection/insert-batch gifts-collection (:gifts data))
  (collection/remove settings-collection)
  (let [settings (update-in (:settings data)
                            [:logo]
                            read-file)]
    (collection/insert settings-collection settings)))

(defn get-setting [key]
  (key (first (collection/find-maps settings-collection ))))

(defn gift-to-dto [gift]
  (-> gift
      (assoc :id (.toString (:_id gift)))
      (dissoc :_id)))

(defn question []
  (get-setting :question))

(defn gifts []
  (->> (collection/find-maps gifts-collection)
       (map gift-to-dto)))

(defn gift [id]
  (-> (collection/find-map-by-id gifts-collection (ObjectId. id))
      gift-to-dto))

(defn reserve [gift-id]
  (let [gift (gift gift-id)]
    (if (= (:reserved gift)
           (:max gift))
      false
      (do (collection/update gifts-collection {:_id (ObjectId. gift-id)} {operators/$inc {:reserved 1}})
          true))))

(defn release [gift-id]
  (let [gift (gift gift-id)]
    (when (> (:reserved gift)
             0)
      (collection/update gifts-collection {:_id (ObjectId. gift-id)} {operators/$inc {:reserved -1}})))
  nil)


(def defaults {:gifts [{:description (apply str (repeat 100 "bla "))
                        :reserved 0
                        :max 1}
                       {:description (apply str (repeat 130 "bla "))
                        :reserved 0
                        :max 3}]
               :settings {:logo "resources/public/images/logo.jpg"
                          :password "foo"
                          :question "Give me foo"
                          :session-key "123jlk12j3"}})
