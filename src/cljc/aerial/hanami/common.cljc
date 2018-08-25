(ns aerial.hanami.common
  (:require
   [com.rpl.specter :as sp]
   [aerial.hanami.templates :as ht]))


(def default-opts
  {:vgl {:export {:png true, :svg false}
         :renderer "canvas" #_"svg"
         :mode "vega-lite" #_vega}
   :layout {:order :col
            :eltsper 2
            :size "none"}})


(def RMV com.rpl.specter/NONE)

(def _defaults
  (atom {:BACKGROUND "floralwhite"
         :TITLE "A Chart", :TOFFSET RMV
         :HEIGHT 500, :WIDTH 550
         :X "x", :XTYPE, "quantitative", :XTITLE RMV, :XSCALE RMV, :XGRID RMV
         :Y "y", :YTYPE, "quantitative", :YTITLE RMV, :YSCALE RMV, :YGRID RMV
         :ENCODING ht/xy-encoding
         :USERDATA RMV
         :TRANSFORM RMV
         :MDWN-MARK {:fill "#333",
                     :fillOpacity 0.125,
                     :stroke "white"},
         :SMDWN-MARK {:fill "#333",
                     :fillOpacity 0.125,
                     :stroke "white"}
         :SELECTION RMV, :ENCODINGS ["x", "y"], :IRESOLVE "global"
         :SIZE RMV
         :COLOR RMV, :XRL-COLOR "red", :YRL-COLOR "green"
         :RESOLVE RMV
         :POINT RMV
         :TOOLTIP ht/default-tooltip
         :RTYPE "quantitative", :AGG RMV}))

(defn reset-defaults [default-map]
  (reset! _defaults default-map))

(defn add-defaults
  ([new-default-map]
   (swap! _defaults (merge @_defaults new-default-map)))
  ([k v & kvs]
   (add-defaults
    (into {k v} (->> kvs (partition-all 2)
                     (mapv (fn[[k v]] [k v])))))))


(defn xform
  ([x xkv]
   (let [defaults @_defaults
         xkv (merge defaults xkv)]
     (sp/transform
      sp/ALL
      (fn[v]
        (if (coll? v)
          (let [xv (xform v xkv)]
            (if (seq xv) xv RMV))
          (let [subval (get xkv v v)]
            #_(clojure.pprint/pprint
             (if (not= v :DATA) [:V v :SUBVAL subval] v))
            (if (or (= v :DATA)
                    (string? subval)
                    (not (coll? subval)))
              subval
              (xform subval xkv)))))
      x)))
  ([x k v & kvs]
   (xform x (into
             {k v}
             (->> kvs (partition-all 2)
                  (mapv (fn[[k v]] [k v])))))))










