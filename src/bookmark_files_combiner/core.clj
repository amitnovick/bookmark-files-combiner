(ns bookmark_files_combiner.core
  (:require [babashka.fs :as fs],
            [babashka.process :refer [shell]],
            [clj-yaml.core :as yaml],
            [clojure.java.io :as io],
            [clojure.pprint :refer [pprint]],
            [clojure.string :as str]))

(def resources-dir-path "/home/amit/Dropbox/dev/clojure/bookmark-files-combiner/resources")

(def bookmark-yaml-files-path (apply str [resources-dir-path, "/bookmark-files"]))

(def pdf-files-path (apply str [resources-dir-path, "/pdf-files"]))

(def output-bookmark-dir-path (apply str [resources-dir-path, "/output-boomark-file"]))

(def yaml-files (sort (fs/list-dir bookmark-yaml-files-path)))

(def pdf-files (sort (fs/list-dir pdf-files-path)))

(defn get-bookmarks []
  (reduce
    (fn [acc, file]
      (with-open [reader (io/reader (io/file (fs/file file)))]
        (conj acc (yaml/parse-stream reader))
        )
      )
    []
    yaml-files
    )
  )

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn get-pdf-files-sizes []
  (reduce
    (fn [acc, file]
      (conj acc
            (parse-int
              (second
                (str/split
                  (first
                    (filter
                      (fn [s] (str/starts-with? s "NumberOfPages:"))
                      (str/split
                        (:out (shell {:out :string}
                                     "pdftk",
                                     (str file),
                                     "dump_data"
                                     )
                          )
                        #"\n"
                        )
                      )
                    )
                  #"\s+"
                  )
                )
              )
            )
      )
    []
    pdf-files
    )
  )

(def pdf-files-sizes (get-pdf-files-sizes))

(def bookmarks (get-bookmarks))   ; map

(defn update-bookmarks [bookmarks]
  (cond
    (< (count bookmarks) 2) bookmarks
    :else (conj (map-indexed

                  (fn [index, file]
                    (map
                      (fn [bookmark-in-file]
                        (assoc bookmark-in-file :BookmarkPageNumber
                                                (+
                                                  (reduce + 0 (subvec pdf-files-sizes 0 (+ index 1)))
                                                  (:BookmarkPageNumber bookmark-in-file)
                                                  )
                                                )
                        )
                      file)
                    )
                  (rest bookmarks)
                  )
                (first bookmarks)
                )
    )
  )

(defn write-bookmarks [bookmarks]
  (with-open [writer (
                       io/writer (
                                   apply str [
                                              output-bookmark-dir-path,
                                              "/bookmarks.txt"
                                              ]
                                         ) :append false)]
    (doseq [file-bookmarks bookmarks]
      (doseq [bookmark file-bookmarks]
        (.write writer (str "BookmarkBegin" "\n"))
        (.write writer (str "BookmarkTitle:" (:BookmarkTitle bookmark) "\n"))
        (.write writer (str "BookmarkLevel:" (:BookmarkLevel bookmark) "\n"))
        (.write writer (str "BookmarkPageNumber:" (:BookmarkPageNumber bookmark) "\n"))
        (.write writer "\n")
        )
      )
    )
  )

(def updated-bookmarks (update-bookmarks bookmarks))

(write-bookmarks updated-bookmarks)

(shell (str/join " " ["pdftk",
       (str/join " "
                 (map
                   (fn [pdf-file-path]
                     (str pdf-file-path)
                     )
                   pdf-files
                   )),
       "cat",
       "output",
       (apply str [output-bookmark-dir-path, "/combined.pdf"]),
       ]))

(shell (str/join " " ["pdftk",
                      (apply str [output-bookmark-dir-path, "/combined.pdf"]),
                      "update_info",
                      (apply str [output-bookmark-dir-path,"/bookmarks.txt"]),
                      "output",
                      (apply str [output-bookmark-dir-path, "/combined-bookmarked.pdf"]),
                      ]
                 ))

(let [temp-file (doto (fs/create-temp-file)
                  (fs/delete-on-exit))]
  temp-file)



