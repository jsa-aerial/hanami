(ns aerial.hanami.data
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clojure.data.json :as json]
   [clojure.string :as str])

  (:import
   java.io.FilenameFilter))



(defn basename [path]
  (.getName (io/file path)))


(defn ftype [path]
  (let [bn (basename path)
        ft (last (str/split bn #"\."))]
    (if (= ft bn) "" ft)))



(defn convert-val [type val]
  (case type
    "string" val
    "int"    (-> val Integer. int)
    "float"  (-> val Float. float)
    "double" (-> val Double. double)
    val))

(defn get-csv
  ([path]
   (let [recs (->> path slurp csv/read-csv)
         [fields & recs] recs
         types (when (-> recs ffirst (= "TYPES")) (-> recs first rest))
         recs (if types (rest recs) recs)]
     (mapv (fn[rec]
             (let [rec (if types (mapv convert-val types rec) rec)]
               (->> rec (mapv vector fields) (into {}))))
           recs)))
  ([path type-vec-or-map]
   (let [recs (->> path slurp csv/read-csv)
         [fields & recs] recs
         types (if (map? type-vec-or-map)
                 (mapv #(type-vec-or-map % "string") fields)
                 type-vec-or-map)]
     (mapv (fn[rec]
             (let [rec (mapv convert-val types rec)]
               (->> rec (mapv vector fields) (into {}))))
           recs))))


(defn get-clj [path]
  (->> path slurp read-string))


(defn get-json [path]
  (-> path slurp (json/read-str :key-fn keyword)))


(defn file-converter [path]
  (let [types {"csv" get-csv, "json" get-json, "clj" get-clj, "edn" get-clj}
        ext (-> path basename ftype)]
    (types ext :unknown)))


(defn get-data
  ([path]
   (let [converter (file-converter path)]
     (converter path)))
  ([path type-vec-or-map]
   (let [converter (file-converter path)]
     (if (not= converter get-csv)
       (do (println :warn "typevec only for csv, ignored")
           (converter path))
       (converter path type-vec-or-map)))))
