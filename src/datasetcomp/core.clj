(ns datasetcomp.core
  (:require [spork.util.table :as tbl]
            [tech.ml.dataset.csv :as tech]
            [datasetcomp.tablesaw :as tablesaw]
            [clj-memory-meter.core :as mm]))

(def the-data (atom nil))
;;atypical def usage, this is for cleaning up resources.
(defn clean! []
  (do (reset! the-data nil) (System/gc)))

(defmacro timed-build [name & expr]
  `(do  (println [:clearing-data-and-gcing])
         (clean!)
         (println [:evaluating ~name :as ~(list 'quote expr)])
         (reset! ~'datasetcomp.core/the-data (time ~@expr))
         (println [:measuring-memory-usage!])
         (println (mm/measure (deref ~'datasetcomp.core/the-data)))))
        
;;Monkey patched a couple of things into spork and tech
(require 'datasetcomp.patches)
;;comparative testing...
(comment
  ;;about 197mb source file.
  ;;2386398 rows, 15 fields, mixture of numeric and text data.
  ;;available at 
  (def events "sampledata.txt") 

  ;;This is doing naive schema inference, with no fallback or widening of the
  ;;type restrictions (it'll fail ungracefully currently, and the inference
  ;;is only based on the first row of data - a dumb hueristic
  (timed-build :spork-table (tbl/tabdelimited->table events))
  ;; [:clearing-data-and-gcing]
  ;; [:evaluating :spork-table :as ((tbl/tabdelimited->table events))]
  ;; "Elapsed time: 8224.751723 msecs"
  ;; [:measuring-memory-usage!]
  ;; 870.8 MB  

  ;;I patched in the ability to pass through separators in tech,
  ;;other than that no modes.  I noticed that tech is holding onto
  ;;the head of a lazy sequence, which puts additional stress on
  ;;the GC (prior records can't be freed and are retained/realized
  ;;during processing).
  
  (timed-build :tech.csv    (tech/csv->dataset events :separator \tab))
  ;; FileNotFoundException sampledata.tx: (No such file or directory)  java.io.FileInputStream.open0 (FileInputStream.java:-2)

  ;;based on the data from the first record, we can use the same heuristic
  ;;to build our table.  This time, if we pass in an explicit, typed
  ;;schema, we should use a different path to create the table.  Namely,
  ;;we end up using primitive columns where possible (i.e. for ints).
  ;;These are based on RRB-Vectors, since normal clojure primitive-backed
  ;;vectors didn't support transients at the time.  If we specify primitives
  ;;in the columns, then we should be able to leverage 
  
  (def schema 
    {:DemandGroup :text,
     :GhostFilled :int,
     :ACFilled :int,
     :SRC :text,
     :TotalRequired :int,
     :DemandName :text,
     :TotalFilled :int,
     :NGFilled :int,
     :Quarter :int,
     :RCFilled :int,
     :t :int,
     :Vignette :text,
     :OtherFilled :int,
     :Overlapping :int,
     :Deployed :int})

  ;;How much savings do we get..?
  (timed-build :spork.util.table-with-schema (tbl/tabdelimited->table events :schema schema))
  ;; [:clearing-data-and-gcing]
  ;; [:evaluating :spork.util.table-with-schema :as ((tbl/tabdelimited->table events :schema schema))]
  ;; "Elapsed time: 12548.208215 msecs"
  ;; [:measuring-memory-usage!]
  ;; 834.7 MB

  ;;Not much.

  ;;a bit faster than spork, but check out the space savings...wow.
  ;;Keep in mind this is also mutable, although many operations in
  ;;the core API appear to do copy-on-write (from cursory scanning),
  ;;so it could be adapted for persistent cases.
  (timed-build :tablesaw
               (tablesaw/->table events :separator \tab))
  ;; [:clearing-data-and-gcing]
  ;; [:evaluating :tablesaw :as ((tablesaw/->table events :separator 	))]
  ;; "Elapsed time: 8622.50875 msecs"
  ;; [:measuring-memory-usage!]
  ;; 74.5 MB
)

