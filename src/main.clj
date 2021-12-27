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
  {:name :echo
   :enter
   (fn [context]
     (let [request (:request context)
           response (ok context)]
       (assoc context :response response)))})

(def json-request-body
  {:name :json-body
   :enter
   (fn [context]
     (let [request (:request context)]
       (assoc context :body-json (body-params/json-parser request))))})

(def json-response
  ; interceptor to take :result and turn it onto a JSON-serialized OK response
  {:name :json-response
   :leave
   (fn [context]
     (if-let [result (:result context)]
       (assoc context :response (ok (json/write-str result)))))
   })

(def get-qso-list
  {:name :get-qso-list
   :leave
   (fn [context]
     (assoc context :result (qsos/all)))})

(def add-qso
  {:name :add-qso
   :enter
   (fn [context]
     (if-let [new-qso (get-in context [:request :son-params])]
       (assoc context :result (qsos/add-qso new-qso))
       (assoc context :response (bad-request "Missing json entity in body"))))})

(def routes
  (route/expand-routes
    #{["/qso"                    :post   [(body-params/body-params) add-qso]]
      ["/qso/new"                :post   [json-request-body json-response add-qso]]
      ["/qso"                    :get    [json-response get-qso-list]]
      ["/qso/:qso-id"            :get    echo :route-name :qso-get]
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