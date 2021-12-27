(ns qsos
  (:require [clojure.java.jdbc :as jdbc]
            [java-jdbc.ddl :as ddl]
            [java-jdbc.sql :as sql]
            [dbconfig]))

(def db dbconfig/db)

(defn create-qsos-table
  "Creates qsos table."Í
  []
  (jdbc/db-do-commands db true
                       (ddl/create-table
                         :qsos
                         [:id :int "PRIMARY KEY" "GENERATED ALWAYS AS IDENTITY"]
                         [:callsign "VARCHAR(20)" "NOT NULL"]
                         [:freq "DECIMAL(12,3)" "NOT NULL"]
                         ;TODO add band
                         ;TODO add mode (and sub-mode?  i.e., digital ft8)
                         [:notes "LONG VARCHAR"]
                         [:started_at :timestamp "NOT NULL"]
                         [:ended_at :timestamp "NOT NULL"]
                         [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn drop-qsos-table
  "Drops qsos table."
  []
  (jdbc/db-do-commands db true (ddl/drop-table :qsos)))

(defn get-qso
  "Get a qso by id"
  [id]
  (jdbc/query db (sql/select * :qsos (sql/where {:id id}))))

(defn add-qso
  "Inserts a new row to the qsos table"
  [new-qso]
  (let [new-ids (jdbc/insert! db :qsos new-qso)]
    (let [new-id ((first new-ids) :1)]
      (get-qso new-id)))
  )

(defn all
  "Gets all rows from the qsos table"
  []
  (jdbc/query db (sql/select * :qsos)))

(defn order-by-created
  "Find column logr.data ordered by the time post was created"
  [column-name]
  (jdbc/query db (sql/select column-name :qsos (sql/order-by :created_at))))

