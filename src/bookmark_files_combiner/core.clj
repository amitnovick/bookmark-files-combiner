#!/usr/bin/env bb

(require '[babashka.fs :as fs])
;(require '[clojure.edn :as edn])


(def paths (fs/list-dir "/home/amit/Dropbox/dev/clojure/bookmark-files-combiner/resources/bookmark-files/"))

(print (count paths))


;(def ENTRIES-LOCATION "entries.edn")
;
;(defn read-entries
;  []
;  (if (fs/exists? ENTRIES-LOCATION)
;    (edn/read-string (slurp ENTRIES-LOCATION))
;    []))
;
;(defn add-entry
;  [text]
;  (let [entries (read-entries)]
;    (spit ENTRIES-LOCATION
;          (conj entries {:timestamp (System/currentTimeMillis)
;                         :entry     text}))))
;
;(add-entry (first *command-line-args*))


