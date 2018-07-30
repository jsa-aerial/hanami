(ns aerial.hanami.core
  (:require
   [clojure.string :as str]
   [ring.middleware.defaults]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [ring.middleware.cljsjs :refer [wrap-cljsjs]]
   [ring.util.response :refer (resource-response content-type)]
   #_[ring.middleware.anti-forgery :refer (*anti-forgery-token*)]

   [compojure.core :as comp :refer (routes GET)]
   [compojure.route :as route]

   [com.rpl.specter :as sp]

   [aerial.hanasu.server :as srv]
   [aerial.hanasu.common :as com]
   [aerial.hanami.common
    :refer [default-opts xform reset-defaults add-defaults]]

   [clojure.data.json :as json]
   [clojure.core.async :as async  :refer (<! >! go-loop)]
   [clojure.java.io :as io])

   (:gen-class))


(def print-chan (async/chan 10))

(go-loop [msg (async/<! print-chan)]
  (println msg)
  (recur (async/<! print-chan)))

(defn printchan [& args]
  (async/put! print-chan (clojure.string/join " " args)))




(defonce app-bpsize 100)
(defonce app-db (atom {:rcvcnt 0 :sntcnt 0}))

(defn update-adb
  ([] (com/update-db app-db {:rcvcnt 0 :sntcnt 0}))
  ([keypath vorf]
   (com/update-db app-db keypath vorf))
  ([kp1 vof1 kp2 vof2 & kps-vs]
   (apply com/update-db app-db kp1 vof1 kp2 vof2 kps-vs)))

(defn get-adb
  ([] (com/get-db app-db []))
  ([key-path] (com/get-db app-db key-path)))




(defn unknown-type-response [ws _]
  (srv/send-msg ws {:op "error" :payload "ERROR: unknown message type"}))

(defn msg-handler [msg]
  (let [{:keys [data ws]} msg
        {:keys [type payload]} data]
    (printchan :DATA data :TYPE type :payload payload)
    ((case type
        unknown-type-response)
     ws payload)))


(defn on-open [ch op payload]
  (let [ws payload
        uid (gensym "hanami-")
        data {:uid uid
              :title "花見 Hanami"
              :logo "logo.png"
              :img "Himeji_sakura.jpg"
              :opts default-opts}]
    (printchan :SRV :open :uid uid)
    (update-adb [uid :ws] ws, [uid :rcvcnt] 0, [uid :sntcnt] 0)
    (srv/send-msg ws {:op :register :data data})))


(defn server-dispatch [ch op payload]
  (case op
    :msg (let [{:keys [ws]} payload]
           (update-adb :rcvcnt inc, [ws :rcvcnt] inc)
           (msg-handler payload))

    :open (on-open ch op payload)
    :close (let [{:keys [ws status]} payload]
             (printchan :SRV :close :payload payload)
             (update-adb ws :rm))

    :bpwait (let [{:keys [ws msg encode]} payload]
              (printchan :SRV "Waiting to send msg " msg)
              (Thread/sleep 5000)
              (printchan :SRV "Trying resend...")
              (srv/send-msg ws msg :encode encode))
    :bpresume (printchan :SRV "BP Resume " payload)

    :sent (let [{:keys [ws msg]} payload]
            (printchan :SRV "Sent msg " msg)
            (update-adb :sntcnt inc, [ws :sntcnt] 0))
    :failsnd (printchan :SRV "Failed send for " {:op op :payload payload})

    :stop (let [{:keys [cause]} payload]
            (printchan :SRV "Stopping reads... Cause " cause)
            (update-adb)
            (srv/stop-server))
    (printchan :SRV :WTF :op op :payload payload)))




(defn landing-handler [request]
  #_(printchan request)
  (content-type
   {:status 200
    :body (io/input-stream (io/resource "public/index.html"))}
   "text/html"))

(def hanami-routes
  (apply routes
         (conj (srv/hanasu-handlers)
               (GET "/" request (landing-handler request))
               (route/resources "/"))))

(def hanami-handler
  (-> hanami-routes
      #_(ring.middleware.defaults/wrap-defaults
       ring.middleware.defaults/site-defaults)
      #_(wrap-cljsjs)
      #_(wrap-gzip)))


(defn start-server [port dispatcher]
  (let [ch (srv/start-server port :main-handler hanami-handler)]
    (printchan "Server start, reading msgs from " ch)
    (update-adb :chan ch)
    (go-loop [msg (<! ch)]
      (let [{:keys [op payload]} msg]
        (future (dispatcher ch op payload))
        (when (not= op :stop)
          (recur (<! ch)))))))

(defn stop-server []
  (async/>!! (get-adb :chan) {:op :stop :payload {:cause :userstop}}))


(defn s! [id op data]
  (srv/send-msg (get-adb [id :ws]) {:op op :data data}))

(defn stabs! [id tabdefs]
  (mapv (fn[tdef]
          (let [specs (->> tdef :specs com/ev (mapv json/write-str))]
            (assoc tdef :specs specs)))
        (com/ev tabdefs)))

(defn svgl!
  ([id vgl-map]
   (s! id :tabs
       {:id :p1 :label "Vis"
        :opts default-opts
        :specs (json/write-str vgl-map)}))
  ([id tid vgl-map]
   (s! id :tabs
       {:id tid :specs vgl-map})))

(defn sopts! [id opts]
  (s! id :opts opts))



(comment

  (start-server 3000 server-dispatch)
  (stop-server)

  )
