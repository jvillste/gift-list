(ns gift-list.web
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [shoreleave.middleware.rpc :as rpc ]
            [ring.util.response :as response]
            [clojure.java.io :as io]
            [ring.middleware.logger :as logger]
            (gift-list [data :as data]
                       [session :as session]
                       api))
  (:use compojure.core
        [ring.adapter.jetty :only [run-jetty]]
        [hiccup core element page])
  (:import org.bson.types.ObjectId))

(defn page-body [title on-load]
  (html5 [:html
          [:head
           [:title title]
           (include-css "css/main.css")
           (include-js "js/javascript-xpath.js")
           (include-js "js/cljs.js")]
          [:body {:onload on-load}]]))

(defn require-session [cookies response]
  (if (session/session-open? cookies)
    (response)
    (response/redirect "/")))

(defn clj->js [x]
  (cond
   (nil? x)
   "null"

   (string? x)
   (str "'" (clojure.string/replace x "\n" "\\n") "'")

   (number? x)
   x

   (map? x)
   (str "{"
        (->> x
             (map (fn [e]
                    (str (if (keyword? (key e))
                           (clojure.string/replace (name (key e)) "-" "_")
                           (key e))
                         ": "
                         (clj->js (val e)))))
             (interpose "," )
             (apply str))
        "}")

   (seq x)
   (str "[" (apply str (interpose "," (vec (map clj->js x))) ) "]")))

(defn list-page []
  (page-body "Lahjalista" (str "gift_list.list.run(" (clj->js {:gifts (data/gifts)
                                                               :logo (data/get-setting :logo)
                                                               :message (data/get-setting :list-message)
                                                               }) ");" )))

(defn login-page []
  (page-body "Lahjalista" (str "gift_list.login.run(" (clj->js {:question (data/get-setting :question)
                                                                :logo (data/get-setting :logo)
                                                                :closing-message (data/get-setting :closing-message)
                                                                }) ");" )))

(defroutes handler
  (GET "/" [] (login-page))
  (GET "/list" {cookies :cookies} (require-session cookies list-page))
  (route/files "/" {:root "resources/public"})
  (route/not-found "Page not found!"))

(def app (-> handler
             (rpc/wrap-rpc)
             (logger/wrap-with-plaintext-logger)
             (handler/site)))

(defn -main [port]
  (data/connect-to-db #_(System/getenv "MONGOLAB_URI"))
  (run-jetty #'app {:port (Integer/parseInt port)
                    :join? false}))

(def server (atom nil))

(defn- run []
  (data/connect-to-db)
  (when @server
    (.stop @server))
  (swap! server (fn [old]
                  (run-jetty #'app {:port 8080 :join? false}))))

(comment
  (run)
  )
