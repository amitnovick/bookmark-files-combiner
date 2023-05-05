(ns bookmark-files-combiner.examples
  (:require [clojure.repl :refer :all]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(foo a)

(def a 3)

(println (+ a 2))
(print "hello" "you")
(println (+ "hi" "there"))

;; by `kennytilton` 02-02-2023
(apropos "redu")

;; by `Martin Půda` 02-02-2023
(count (filter #(and  (:arglists (meta %))
                      (not (:macro (meta %))))
               (vals (ns-publics 'clojure.core)))) ;; 546

;; by `delaguardo` 02-02-2023
(count (filter (comp (some-fn ifn? fn?) deref)
               (vals (ns-publics 'clojure.core)))) ;; 636

(vals (ns-publics 'clojure.core))
(filter function? (ns-publics 'clojure.core))

(ns-publics 'clojure.core)
(type (ns-publics 'clojure.core))

(type (vals (ns-publics 'clojure.core)))

(count
  (vals (ns-publics 'clojure.core)))

(ns-map 'clojure.core)
(clojure.repl/source symbol)
