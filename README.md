# qso-logr

REST API to persist Ham Radio QSOs (contacts).  Stores some very basic information in a Derby database and uses Pedestal.io for the web service stuff.

## APIs

|Method|Path|Description|
|------|----|-----------|
|`POST`|`/qso`|Add a new QSO and return the newly added entry|
|`GET`|`/qso`|Retrieve all QSOs in the database|
|`GET`|`/qso/[id]`|Retrieve a single QSO entry|

QSO JSON structure

````JSON
{
  "id": 1,
  "callsign": "KF3W",
  "freq": 14.225,
  "notes": "hello world!",
  "started_at": "2021-12-26T16:11:11Z",
  "ended_at": "2021-12-26T17:12:12Z",
  "created_at":"2021-12-26T15:50:44.436Z"
}
````
`created_at` is returned but not needed for post data to add a new entry.


### Disclaimer

*This was the result of a couple days of playing with and learning about Clojure.  Having never done any Clojure (or any Lisp-like languages) before, I'm sure it's at least a little bit terrible, but I learned a lot and had fun.*
