(ns aerial.hanami.common
  (:require
   [com.rpl.specter :as sp]
   [aerial.hanami.templates :as ht]))


(def default-opts
  {:vgl {:export {:png true, :svg false}
         :renderer "canvas" #_"svg"
         :mode "vega-lite" #_vega}
   :tab {:order :row
         :eltsper 2
         :size "auto"}})


(def RMV com.rpl.specter/NONE)
(def data-key :DATA)
(def color-key :COLOR)
(def shape-key :SHAPE)
(def title-key :TITLE)


#?(:clj
   (do
     (defn set-data-key! [key]
       (alter-var-root (var data-key) (constantly key)))
     (defn set-color-key! [key]
       (alter-var-root (var color-key) (constantly key)))
     (defn set-shape-key! [key]
       (alter-var-root (var shape-key) (constantly key)))
     (defn set-title-key! [key]
       (alter-var-root (var title-key) (constantly key))))
   :cljs
   (do
     (defn set-data-key!  [key] (set! data-key  key))
     (defn set-color-key! [key] (set! color-key key))
     (defn set-shape-key! [key] (set! shape-key key))
     (defn set-title-key! [key] (set! title-key key))))


;;; Subkeyfns may well make use of xform - defaults definitely do
(declare xform)

(def subkeyfns
  (atom {color-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))

         shape-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))

         title-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-title (assoc xkv :TTEXT subval))
             subval))

         ;; Hack for broken VGL 3.0.0-rc6 - incorrect grouping by TT fields
         :TOOLTIP
         (fn[xkv subkey subval]
           (when-let [spec (xkv ::spec)]
             (let [mark (spec :mark)
                   mtype (cond (map? mark) (mark :type)
                               (string? mark) mark
                               (keyword? mark) (get xkv mark)
                               :else nil)
                   point (when (map? mark) (get xkv :POINT))]
               (println :POINT point :MTYPE mtype)
               (if (and (not= point true) (#{:line "line"} mtype))
                 RMV
                 subval))))}))

(def _defaults
  (atom {;; General
         :BACKGROUND "floralwhite"
         :TITLE RMV, :TOFFSET RMV
         :HEIGHT 300, :WIDTH 400
         :USERDATA RMV

         ;; Data, transforms, and encodings
         :DATA RMV, :UDATA RMV, :NDATA RMV
         :TRANSFORM RMV
         :OPACITY RMV
         :AGG RMV, :XAGG RMV, :YAGG RMV
         :X "x", :XTYPE, "quantitative", :XUNIT RMV
         :XSCALE RMV
         :XTITLE RMV, :XGRID RMV, :XFORMAT RMV
         :Y "y", :YTYPE, "quantitative", :YUNIT RMV
         :YSCALE RMV
         :YTITLE RMV, :YGRID RMV, :YFORMAT RMV
         :ROWDEF ht/default-row :ROW RMV, :ROWTYPE RMV
         :COLDEF ht/default-col :COLUMN RMV, :COLTYPE RMV
         :POINT RMV, :MSIZE RMV
         :TOOLTIP ht/default-tooltip
         :ENCODING ht/xy-encoding
         :RESOLVE RMV
         :XRL-COLOR "red", :YRL-COLOR "green"
         :RTYPE "quantitative"

         ;; Selections
         :MDWN-MARK {:fill "#333",
                     :fillOpacity 0.125,
                     :stroke "white"},
         :SMDWN-MARK {:fill "#333",
                     :fillOpacity 0.125,
                     :stroke "white"}
         :SELECTION RMV, :ENCODINGS ["x", "y"], :IRESOLVE "global"

         ;; Mark Properties
         :MPTYPE "nominal"
         :SHAPE RMV
         :SIZE RMV
         ;; color
         :COLOR RMV
         :CFIELD :X, :CTYPE :XTYPE, :LTYPE "symbol" :LTITLE "" :LOFFSET 0
         :CSCALE {:scheme {:name "greenblue" :extent [0.4 1]}}
         :CLEGEND {:type :LTYPE :offset :LOFFSET :title :LTITLE}
         }))

(defn reset-defaults [default-map]
  (reset! _defaults default-map))

(defn add-defaults
  ([new-default-map]
   (swap! _defaults #(merge % new-default-map)))
  ([k v & kvs]
   (add-defaults
    (into {k v} (->> kvs (partition-all 2)
                     (mapv (fn[[k v]] [k v])))))))

(defn get-default [k] (@_defaults k))



(defn xform
  ([spec xkv]
   (let [xkv (if (not (xkv ::spec)) (assoc xkv ::spec spec) xkv)
         defaults @_defaults
         xkv (merge defaults xkv)]
     (sp/transform
      sp/ALL
      (fn[v]
        (if (coll? v)
          (let [xv (xform v xkv)]
            (if (seq xv) xv RMV))
          (let [subval (get xkv v v)
                subval (if (fn? subval) (subval xkv) subval)
                subkeyfn (@subkeyfns v)
                subval (if subkeyfn (subkeyfn xkv v subval) subval)]
            #_(clojure.pprint/pprint
               (if (not= v data-key) [v :SUBVAL subval] v))
            (cond
              ;; leaf value => termination
              (= v subval) v

              ;; Do not xform the data
              (= v data-key) subval

              ;; Potential new subkey as subval
              (or (string? subval)
                  (not (coll? subval)))
              (recur subval)

              :else ;substitution val is coll
              (let [xv (xform subval xkv)]
                (if (seq xv) xv RMV))))))
      spec)))

  ([spec k v & kvs]
   (xform spec (into
                {k v}
                (->> kvs (partition-all 2)
                     (mapv (fn[[k v]] [k v]))))))

  ([spec] (xform spec {})))










