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
   [aerial.hanami.templates :as ht]

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




(defn uuid [] (str (java.util.UUID/randomUUID)))

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
        uid-name ((-> :idfn get-adb first))
        uuid (uuid)
        uid {:uuid uuid :name uid-name}
        data {:uid uid
              :title (get-adb [:header :title])
              :logo (get-adb [:header :logo])
              :img (get-adb [:header :img])
              :opts default-opts}
        name-uuids (or (get-adb uid-name) [])]
    (printchan :SRV :open uid)
    (update-adb [uuid :ws] ws, [uuid :name] uid-name
                [uuid :rcvcnt] 0, [uuid :sntcnt] 0
                [ws :uuid] uuid
                uid-name (conj name-uuids uuid))
    (srv/send-msg ws {:op :register :data data})))


(defn server-dispatch [ch op payload]
  (case op
    :msg (let [{:keys [ws]} payload]
           (update-adb :rcvcnt inc, [ws :rcvcnt] inc)
           (msg-handler payload))

    :open (on-open ch op payload)
    :close (let [{:keys [ws status]} payload
                 uuid (get-adb [ws :uuid])
                 uuid-name (get-adb [uuid :name])
                 uuids (get-adb uuid-name)]
             (printchan :SRV :close :uuid uuid uuid-name)
             (update-adb ws :rm, uuid :rm
                         uuid-name (->> uuids (remove #(= uuid %)) vec)))

    :bpwait (let [{:keys [ws msg encode]} payload
                  uuid (get-adb [ws :uuid])]
              (printchan :SRV :UUID uuid " - Waiting to send msg " msg)
              (update-adb [uuid :bpdata] {:msg msg, :encode encode}))
    :bpresume (let [{:keys [ws msg]} payload
                    uuid (get-adb [ws :uuid])
                    encode ((get-adb [uuid :bpdata]) :encode)]
                (printchan :SRV "BP Resume " (get-adb [ws :uuid]))
                (srv/send-msg ws msg :encode encode))

    :sent (let [{:keys [ws msg]} payload]
            #_(printchan :SRV "Sent msg " msg)
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


(defn start-server
  [port & {:keys [idfn title logo img]
           :or {idfn (partial gensym "hanami-")
                title "花見 Hanami"
                logo "logo.png"
                img "Himeji_sakura.jpg"}}]
  (let [ch (srv/start-server port :main-handler hanami-handler)]
    (printchan "Server start, reading msgs from " ch)
    (update-adb :chan ch :idfn [idfn]
                [:header :title] title
                [:header :logo] logo
                [:header :img] img)
    (go-loop [msg (<! ch)]
      (let [{:keys [op payload]} msg]
        (future (server-dispatch ch op payload))
        (when (not= op :stop)
          (recur (<! ch)))))))

(defn stop-server []
  (async/>!! (get-adb :chan) {:op :stop :payload {:cause :userstop}}))


(defn s! [uuids op data]
  (doseq [id uuids]
    (srv/send-msg (get-adb [id :ws]) {:op op :data data})))

(defn stabs! [uuid-name tabdefs]
  (s! (get-adb uuid-name) :tabs (com/ev tabdefs)))

(defn svgl!
  ([uuid-name vgl-maps]
   (s! (get-adb uuid-name) :tabs
       {:id :p1 :label "Vis"
        :opts default-opts
        :specs (com/ev vgl-maps)}))
  ([uuid-name tid vgl-maps]
   (s! (get-adb uuid-name) :tabs
       {:id tid
        :label (-> tid name str/capitalize)
        :specs (com/ev vgl-maps)})))

(defn sopts!
  ([uuid-name opts]
   (s! (get-adb uuid-name) :opts {:main opts}))
  ([uuid-name tid opts]
   (s! (get-adb uuid-name) :opts {:tab tid :opts opts})))



(comment

  (start-server 3003 :idfn (constantly "Basics"))
  (stop-server)

  )
