(ns bookmark_files_combiner.core
  (:require [babashka.fs :as fs],
            [babashka.process :refer [shell]],
            [clj-yaml.core :as yaml],
            [clojure.java.io :as io],
            [clojure.pprint :refer [pprint]],
            [clojure.string :as str]))

(defn get-bookmarks [yaml-files]
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

(defn get-pdf-files-sizes [pdf-files]
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
                                     file,
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

(defn update-bookmarks [bookmarks, pdf-files-sizes]
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

(defn write-bookmarks [bookmarks output-bookmark-txt-path]
  (with-open [writer (
                       io/writer output-bookmark-txt-path :append false)]
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

(defn merge-pdfs [output-combined-pdf-path pdf-files]
  (apply shell "pdftk"
         (concat
           (map str pdf-files)
           ["cat" "output" output-combined-pdf-path]
           )
         )
  )

(defn add-bookmarks-to-merged-pdf [output-bookmark-txt-path,
                                   output-combined-pdf-path,
                                   output-bookmarked-pdf-path]
  (shell (str/join " " ["pdftk",
                        output-combined-pdf-path,
                        "update_info",
                        output-bookmark-txt-path,
                        "output",
                        output-bookmarked-pdf-path
                        ]
                   ))
  )

(let [[pdfs-dir-path-string
       bookmarks-yaml-files-dir-path-strings
       output-pdf-dir-path-string
       ] *command-line-args*
      temp-bookmark-txt-path (doto (fs/create-temp-file)
                               (fs/delete-on-exit))
      temp-combined-pdf-path (doto (fs/create-temp-file)
                               (fs/delete-on-exit))]
  (def pdfs-dir-path (fs/absolutize (fs/path pdfs-dir-path-string)))
  (def bookmarks-yaml-files-dir-path (fs/absolutize (fs/path bookmarks-yaml-files-dir-path-strings)))
  (def output-pdf-dir-path (fs/absolutize (fs/path output-pdf-dir-path-string)))
  (def bookmark-yaml-files-path (str bookmarks-yaml-files-dir-path))
  (def yaml-files (sort (fs/list-dir bookmark-yaml-files-path)))
  (def bookmarks (get-bookmarks yaml-files))
  (def pdf-files-path (str pdfs-dir-path))
  (def pdf-files (sort (fs/list-dir pdf-files-path)))
  (def pdf-files-sizes (get-pdf-files-sizes pdf-files))
  (def updated-bookmarks (update-bookmarks bookmarks pdf-files-sizes))

  (write-bookmarks updated-bookmarks (str temp-bookmark-txt-path))
  (merge-pdfs (str temp-combined-pdf-path) pdf-files)
  (add-bookmarks-to-merged-pdf
    (str temp-bookmark-txt-path)
    (str temp-combined-pdf-path)
    (apply str [output-pdf-dir-path, "/combined-bookmarked.pdf"])
    )
  )