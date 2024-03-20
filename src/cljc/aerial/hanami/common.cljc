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
   :tab {:order :ORDER       ; either :row or :col - see defaults
         :eltsper :ELTSPER   ; count of elements per row/col - see defaults
         :rgap :RGAP         ; default gap between rows
         :cgap :CGAP         ; default gap between columns
         :size "auto"}})


(def RMV com.rpl.specter/NONE)
(def data-key :DATA)
(def fdata-key :FDATA)
(def color-key :COLOR)
(def strokedash-key :SDASH)
(def shape-key :SHAPE)
(def opacity-key :OPACITY)
(def size-key :SIZE)
(def title-key :TITLE)


#?(:clj
   (do
     (defn set-data-key! [key]
       (alter-var-root (var data-key) (constantly key)))
     (defn set-fdata-key! [key]
       (alter-var-root (var fdata-key) (constantly key)))
     (defn set-color-key! [key]
       (alter-var-root (var color-key) (constantly key)))
     (defn set-strokedash-key! [key]
       (alter-var-root (var strokedash-key) (constantly key)))
     (defn set-shape-key! [key]
       (alter-var-root (var shape-key) (constantly key)))
     (defn set-opacity-key! [key]
       (alter-var-root (var opacity-key) (constantly key)))
     (defn set-size-key! [key]
       (alter-var-root (var size-key) (constantly key)))
     (defn set-title-key! [key]
       (alter-var-root (var title-key) (constantly key))))
   :cljs
   (do
     (defn set-data-key!  [key] (set! data-key  key))
     (defn set-fdata-key! [key] (set! fdata-key key))
     (defn set-color-key! [key] (set! color-key key))
     (defn set-strokedash-key! [key] (set! strokedash-key key))
     (defn set-shape-key! [key] (set! shape-key key))
     (defn set-opacity-key! [key] (set! opacity-key key))
     (defn set-size-key! [key] (set! size-key key))
     (defn set-title-key! [key] (set! title-key key))))


;;; Subkeyfns may well make use of xform - defaults definitely do
(declare xform)

(def subkeyfns
  (atom {color-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))

         strokedash-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))

         shape-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))

         opacity-key
         (fn[xkv subkey subval]
           (cond
             (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))

             (number? subval) {:value subval}
             :else subval))

         size-key
         (fn[xkv subkey subval]
           (cond
             (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))

             (number? subval) {:value subval}
             :else subval))

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

                      :else (data/get-data subval)))])}))

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
  (atom {;; Controls
         ::rmv-empty? true ::use-defaults? true

         ;; General
         :BACKGROUND "floralwhite"
         :TITLE RMV, :TOFFSET RMV
         :HEIGHT 300, :WIDTH 400, :DHEIGHT 60
         :CFGBAR RMV, :CGFVIEW RMV, :CFGAXIS RMV, :CFGRANGE RMV
         :USERDATA RMV, :MODE "vega-lite", :RENDERER "canvas" :SCALEFACTOR 1
         :ORDER :row, :ELTSPER 2, :RGAP "20px", :CGAP "20px"

         :TOP RMV, :BOTTOM RMV, :LEFT RMV, :RIGHT RMV
         :VID RMV :FID RMV :AT RMV :POS RMV

         ;; Data and transforms
         :VALDATA get-data-vals
         :DATASETS RMV
         :DATA RMV, :FDATA RMV, :SDATA RMV, :UDATA RMV, :NDATA RMV :DFMT RMV
         :TRANSFORM RMV
         :AGG RMV, :XAGG RMV, :YAGG RMV

         ;; encodings
         :X "x", :XTYPE "quantitative", :XBIN RMV, :XUNIT RMV, :XSORT RMV
         :XSCALE RMV, :XSTACK RMV
         :XAXIS {:title :XTITLE, :grid :XGRID, :format :XFORMAT}
         :XTITLE RMV, :XGRID RMV, :XFORMAT RMV, :XTTITLE RMV, :XTFMT RMV
         :Y "y", :YTYPE, "quantitative", :YBIN RMV, :YUNIT RMV, :YSORT RMV
         :YSCALE RMV, :YSTACK RMV
         :YAXIS {:title :YTITLE, :grid :YGRID, :format :YFORMAT}
         :YTITLE RMV, :YGRID RMV, :YFORMAT RMV, :YTTITLE RMV, :YTFMT RMV
         :X2TYPE (fn [ctx]
                   (if (-> ctx :X2 (not= RMV))
                     (:XTYPE ctx)
                     RMV))
         :Y2TYPE (fn [ctx]
                   (if (-> ctx :Y2 (not= RMV))
                     (:YTYPE ctx)
                     RMV))
         :X2ENCODING (fn [ctx]
                       (if (-> ctx :X2 (not= RMV))
                         (-> ht/xy-encoding
                             :x
                             (assoc :field :X2
                                    :type :X2TYPE))
                         RMV))
         :Y2ENCODING (fn [ctx]
                       (if (-> ctx :Y2 (not= RMV))
                         (-> ht/xy-encoding
                             :y
                             (assoc :field :Y2
                                    :type :Y2TYPE))
                         RMV))
         :TXT RMV, :TTYPE RMV, :TAXIS RMV, :TSCALE RMV
         :ROWDEF ht/default-row :ROW RMV, :ROWTYPE RMV
         :COLDEF ht/default-col :COLUMN RMV, :COLTYPE RMV
         :POINT RMV
         :ORDER> RMV
         :COLOR RMV
         :OPACITY RMV
         :SHAPE RMV, :SIZE RMV
         :STROKE RMV, :SDASH RMV
         :ENCODING ht/xy-encoding
         :RESOLVE RMV
         :XRL-COLOR "red", :YRL-COLOR "green"
         :RTYPE "quantitative"

         ;; tooltips
         :TOOLTIP RMV ;;; No longer use ht/default-tooltip, see MTOOLTIP
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
         :MSIZE RMV
         :MCOLOR RMV
         :MFILLED RMV
         :MPSCALE RMV
         :MSTROKE RMV, :MSDASH RMV
         :MTOOLTIP true ; VGL default for all fields in encoding
         ;; color
         :CFIELD :X, :CTYPE :XTYPE, :LTYPE "symbol" :LTITLE "" :LOFFSET 0
         :CSCALE {:scheme {:name "greenblue" :extent [0.4 1]}}
         :CLEGEND {:type :LTYPE :offset :LOFFSET :title :LTITLE}
         ;; text
         :DX RMV, :DY RMV, :XOFFSET RMV, :YOFFSET RMV
         :ANGLE RMV, :ALIGN RMV, :BASELINE RMV
         :FONT RMV, :FONTSIZE RMV, :LIMIT RMV
         :FONTSTYLE RMV, :FONTWEIGHT RMV, :TCOLOR RMV
         :LINEHEIGHT RMV

         ;; Vega layout transforms
         :KEY "id", :PARENTKEY "parent", :NAME "name"
         :LAYOUT "tidy", :ORIENT "horizontal", :LINKSHAPE "diagonal"
         :TREESIZE [{:signal "height"} {:signal "width - 100"}]
         :TREEAS ["y" "x" "depth" "children"]
         :CSCHEME "greenblue"
         :SIGNALS RMV

         ;; stroke short cuts
         :solid [1 0], "-" :solid
         :dashed [10 10], "--" :dashed
         :dashdot [2 5 5 5], "-." :dashdot
         :dot [2 2], ":" :dot
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
      (if (= v ::RMV)
        (swap! _defaults #(dissoc % k))
        (sp/setval [sp/ATOM k] v _defaults))))))

(defn get-default [k] (@_defaults k))
(defn get-defaults [k & ks]
  (if (seq ks)
    (mapv #(vector % (@_defaults %)) (cons k ks))
    (get-default k)))

;;; Not properly general - use update-defaults
(defn add-defaults
  "Deprecated - use update-defaults"
  ([new-default-map]
   (swap! _defaults #(merge % new-default-map)))
  ([k v & kvs]
    (->> kvs (partition-all 2) (mapv vec) (into {k v}) add-defaults)))




(defn xform
  ([spec xkv]
   (let [xkv (if (not (xkv ::spec)) (assoc xkv ::spec spec) xkv)
         defaults @_defaults
         use-defaults? (get xkv ::use-defaults? (defaults ::use-defaults?))
         xkv (if use-defaults? (merge defaults xkv) xkv)
         template-defaults (if (map? spec) (spec ::ht/defaults) false)
         spec (if template-defaults (dissoc spec ::ht/defaults) spec)
         xkv (if template-defaults
               (merge xkv template-defaults (xkv ::user-kvs))
               xkv)]
     (sp/transform
      sp/ALL
      (fn[v]
        (if (coll? v)
          (let [xv (xform v xkv)
                rmv? (xkv ::rmv-empty?)]
            (if (seq xv) xv (if rmv? RMV xv)))
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
              (let [xv (xform subval xkv)
                    rmv? (xkv ::rmv-empty?)]
                (if (seq xv) xv (if rmv? RMV xv)))))))
      spec)))

  ([spec k v & kvs]
   (let [user-kv-map (into {k v}
                        (->> kvs (partition-all 2)
                             (mapv (fn[[k v]] [k v]))))
         ;; Need to keep these to override new template defaults
         start-kv-map (assoc user-kv-map ::user-kvs user-kv-map)]
     (xform spec start-kv-map)))

  ([spec] (xform spec {})))
