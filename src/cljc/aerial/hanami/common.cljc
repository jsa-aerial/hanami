(ns aerial.hanami.common
  (:require
   [com.rpl.specter :as sp]
   [aerial.hanami.templates :as ht]
   #?(:clj [aerial.hanami.data :as data])))


(def default-opts
  {:vgl {:export {:png true, :svg true}
         :scaleFactor :SCALEFACTOR
         :editor true
         :source false
         :renderer :RENDERER ; either "canvas" or "svg" - see defaults
         :mode :MODE}        ; either "vega-lite" or "vega" - see defaults
   :tab {:order :row
         :eltsper 2
         :size "auto"}})


(def RMV com.rpl.specter/NONE)
(def data-key :DATA)
(def fdata-key :FDATA)
(def color-key :COLOR)
(def shape-key :SHAPE)
(def title-key :TITLE)


#?(:clj
   (do
     (defn set-data-key! [key]
       (alter-var-root (var data-key) (constantly key)))
     (defn set-fdata-key! [key]
       (alter-var-root (var fdata-key) (constantly key)))
     (defn set-color-key! [key]
       (alter-var-root (var color-key) (constantly key)))
     (defn set-shape-key! [key]
       (alter-var-root (var shape-key) (constantly key)))
     (defn set-title-key! [key]
       (alter-var-root (var title-key) (constantly key))))
   :cljs
   (do
     (defn set-data-key!  [key] (set! data-key  key))
     (defn set-fdata-key! [key] (set! fdata-key key))
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

         #?@(:clj
             [fdata-key
              (fn [xkv subkey subval]
                (cond (= subval RMV) RMV

                      (vector? subval)
                      (data/get-data (first subval) (second subval))

                      :else (data/get-data subval)))])

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
               (if (and (not= point true) (#{:line "line"} mtype))
                 RMV
                 subval))))}))

(defn update-subkeyfns
  ([new-subkeyfn-map]
   (swap! subkeyfns #(merge % new-subkeyfn-map)))
  ([k vfn & kvfns]
   (update-subkeyfns
    (doseq [[k vfn] (-> k (cons (cons vfn kvfns)) (->> (partition-all 2)))]
      (sp/setval [sp/ATOM k] vfn subkeyfns)))))


(defn get-data-vals [xkv]
  (let [data (xkv data-key)
        fdata (xkv fdata-key)
        fdatafn (@subkeyfns fdata-key)]
    (cond (not= data RMV) data
          (not= fdata RMV) (if fdatafn (fdatafn xkv fdata-key fdata) RMV)
          :else RMV)))

(def _defaults
  (atom {;; General
         :BACKGROUND "floralwhite"
         :TITLE RMV, :TOFFSET RMV
         :HEIGHT 300, :WIDTH 400, :DHEIGHT 60
         :USERDATA RMV, :MODE "vega-lite", :RENDERER "canvas" :SCALEFACTOR 1
         :TOP RMV, :BOTTOM RMV, :LEFT RMV, :RIGHT RMV

         ;; Data and transforms
         :VALDATA get-data-vals
         :DATA RMV, :FDATA RMV, :SDATA RMV, :UDATA RMV, :NDATA RMV
         :TRANSFORM RMV
         :OPACITY RMV
         :AGG RMV, :XAGG RMV, :YAGG RMV

         ;; encodings
         :X "x", :XTYPE, "quantitative", :XUNIT RMV, :XSORT RMV
         :XSCALE RMV, :XAXIS {:title :XTITLE, :grid :XGRID, :format :XFORMAT}
         :XTITLE RMV, :XGRID RMV, :XFORMAT RMV, :XTTITLE RMV, :XTFMT RMV
         :Y "y", :YTYPE, "quantitative", :YUNIT RMV, :YSORT RMV
         :YSCALE RMV, :YAXIS {:title :YTITLE, :grid :YGRID, :format :YFORMAT}
         :YTITLE RMV, :YGRID RMV, :YFORMAT RMV, :YTTITLE RMV, :YTFMT RMV
         :ROWDEF ht/default-row :ROW RMV, :ROWTYPE RMV
         :COLDEF ht/default-col :COLUMN RMV, :COLTYPE RMV
         :POINT RMV, :MSIZE RMV, :MCOLOR RMV, :MFILLED RMV
         :ENCODING ht/xy-encoding
         :RESOLVE RMV
         :XRL-COLOR "red", :YRL-COLOR "green"
         :RTYPE "quantitative"

         ;; tooltips
         :TOOLTIP ht/default-tooltip
         :XTTIP #(xform ht/ttdef
                        :TTFIELD (% :X) :TTTYPE (% :XTYPE)
                        :TTTITLE (% :XTTITLE) :TTFMT (% :XTFMT))
         :YTTIP #(xform ht/ttdef
                        :TTFIELD (% :Y) :TTTYPE (% :YTYPE)
                        :TTTITLE (% :YTTITLE) :TTFMT (% :YTFMT))

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

         ;; Vega layout transforms
         :KEY "id", :PARENTKEY "parent", :NAME "name"
         :LAYOUT "tidy", :ORIENT "horizontal", :LINKSHAPE "diagonal"
         :TREESIZE [{:signal "height"} {:signal "width - 100"}]
         :TREEAS ["y" "x" "depth" "children"]
         :CSCHEME "greenblue"
         :FONTSIZE 9, :BASELINE "middle"
         :SIGNALS RMV
         }))

;;; :SIGNALS
;;; [{:name "labels", :value true, :bind {:input "checkbox"}}]
;;; :OPACITY {:signal "labels ? 1 : 0"}


(defn reset-defaults [default-map]
  (reset! _defaults default-map))

(defn update-defaults
  "Add, change, or remove substitution keys and their substitution
  values from main substitution map"
  ([new-defaults-map]
   (swap! _defaults #(merge % new-defaults-map)))
  ([k v & kvs]
   (update-defaults
    (doseq [[k v] (-> k (cons (cons v kvs)) (->> (partition-all 2)))]
      (sp/setval [sp/ATOM k] v _defaults)))))

(defn get-default [k] (@_defaults k))

;;; Not properly general - use update-defaults
(defn add-defaults
  "Deprecated - use update-defaults"
  ([new-default-map]
   (swap! _defaults #(merge % new-default-map)))
  ([k v & kvs]
   (add-defaults
    (into {k v} (->> kvs (partition-all 2)
                     (mapv (fn[[k v]] [k v])))))))




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










