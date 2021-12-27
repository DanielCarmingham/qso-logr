(ns dbconfig)

(def db-path "./logr-db") ; a path to the database

(def db {
         :subprotocol "derby"
         :subname db-path
         :create true})  ; database setups