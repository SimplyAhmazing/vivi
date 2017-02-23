(ns vivi.core
  (:require
    [immutant.web             :as web]
    [immutant.web.async       :as async]
    [immutant.web.middleware  :as web-middleware]
    [compojure.route          :as route]
    [environ.core             :refer (env)]
    [compojure.core           :refer (ANY GET defroutes)]
    [ring.util.response       :refer (response redirect content-type)]
    [clojure.data.json        :as json])
  (:use
    [ring.middleware.reload]
    [clojure.pprint])
  (:gen-class))


(def rooms-table (atom {}))  ; {'room-uuid': [array of conns objs...]})

(defn add-to-room [room conn]
  (swap! rooms-table assoc (keyword room) (conj ((keyword room) @rooms-table) conn))
  (clojure.pprint/pprint rooms-table))


(defn remove-from-room [room conn]
  (swap! rooms-table assoc (keyword room) (remove #(= % conn) ((keyword room) @rooms-table)))
  (clojure.pprint/pprint rooms-table))


(defn broadcast-to-room [room sender message]
  (apply #(async/send! % (json/write-str message)) ((keyword room) @rooms-table)))


(defn validate-packet [packet]
  (println "Pretending packet is valid" packet))


(defn parse-message [room ch message]
  (def packet (json/read-str message))
  (validate-packet packet)
  (broadcast-to-room room ch packet)
  ; (async/send! ch (json/write-str packet))
  )


(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open   (fn [channel]
                (add-to-room "foo1" channel)
                (async/send! channel "Ready to reverse your messages!"))
   :on-close   (fn [channel {:keys [code reason]}]
                 (remove-from-room "foo1" channel)
                 (println "close code:" code "reason:" reason))
   :on-message (fn [ch m]
                 (println "Channel: " ch "..With message: " m)
                 (parse-message "foo1" ch m))})


(defroutes routes
    (GET "/" {c :context} (redirect (str c "/index.html")))
      (route/resources "/"))


(defn -main [& {:as args}]
  (web/run
    (-> routes
        (web-middleware/wrap-session {:timeout 20})
        ;; wrap the handler with websocket support
        ;; websocket requests will go to the callbacks, ring requests to the handler
        (wrap-reload 'vivi.core)
        (web-middleware/wrap-websocket websocket-callbacks))
    (merge {"host" (env :vivi-web-host), "port" (env :vivi-web-port)}
           args)))
