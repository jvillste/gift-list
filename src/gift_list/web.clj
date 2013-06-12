(ns gift-list.web
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [shoreleave.middleware.rpc :as rpc ]
            [ring.util.response :as response]
            [clojure.java.io :as io]
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
           (include-js "js/cljs.js")]
          [:body {:onload on-load}]]))

(defn require-session [cookies response]
  (if (session/session-open? cookies)
    (response)
    (response/redirect "/")))

(defn list-page []
  (page-body "Lahjalista" "gift_list.list.run();"))

(defn login-page []
  (page-body "Lahjalista" "gift_list.login.run();"))

(defn logo []
  {:status 200
   :headers {"Content-Type" "image/jpeg"}
   :body (io/input-stream (data/get-setting :logo))})

(defroutes handler
  (GET "/logo" [] (logo))
  (GET "/" [] (login-page))
  (GET "/list" {cookies :cookies} (require-session cookies list-page))
  (route/files "/" {:root "resources/public"})
  (route/not-found "Page not found!"))

(def app (-> handler
             (rpc/wrap-rpc)
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