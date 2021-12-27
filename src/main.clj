(ns main
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.test :as test]
            [qsos :as qsos]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok          (partial response 200))
(def created     (partial response 201))
(def accepted    (partial response 202))
(def bad-request (partial response 400))

(def echo
  ; interceptor for troubleshooting
  {:name :echo
   :enter
   (fn [context]
     (let [request (:request context)
           response (ok context)]
       (assoc context :response response)))})

(def json-request-body
  ; interceptor to parse JSON from the body and put it in :request :json-params
  {:name :json-body
   :enter
   (fn [context]
     (let [request (:request context)]
       (assoc-in context [:request] (body-params/json-parser request))))})

(def json-response
  ; interceptor to take :result from context and turn it onto a JSON-serialized OK response
  {:name :json-response
   :leave
   (fn [context]
     (if-let [result (::result context)]
       (assoc context :response (ok (json/write-str result)))
       context))
   })

(def get-qso-list
  {:name :get-qso-list
   :leave
   (fn [context]
     (assoc context ::result (qsos/all)))})

(def add-qso
  {:name :add-qso
   :enter
   (fn [context]
     (if-let [json-params (get-in context [:request :json-params])]
       (assoc context ::result (qsos/add-qso json-params))
       (assoc context :response (bad-request "Missing json entity in body")))
     )})

(def get-qso
  {:name :get-qso
   :enter
   (fn [context]
     (let [qso-id (get-in context [:request :path-params :qso-id])]
       (assoc context ::result (qsos/get-qso qso-id))))})

(def routes
  (route/expand-routes
    #{["/qso"                    :post   [json-request-body json-response add-qso]]
      ["/qso"                    :get    [json-response get-qso-list]]
      ["/qso/:qso-id"            :get    [json-response get-qso]]
      ["/qso/:qso-id"            :put    echo :route-name :qso-update]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})

(defn start []
  (http/start (http/create-server service-map)))

;; For interactive development
(defonce server (atom nil))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                        (assoc service-map
                          ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

(defn test-request [verb url]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url))