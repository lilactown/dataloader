(ns dataloader.core)

(defprotocol ILoader
  (-batch [this key]
    "Adds a key to the queue to be run in the next batch")
  (-pending? [this]
    "Are there any keys pending to be run?")
  (-put [this key value]
    "Puts a value in the cache.")
  (-run [this]
    "Runs all queued keys as a batch"))

(deftype AsyncLoader [cache batch-queue batch-fn]
  ILoader
  (-batch [this key]
    (swap! batch-queue conj key))
  (-pending? [this]
    (not (empty? @batch-queue)))
  (-put [this key value]
    (swap! cache assoc key value))
  (-run [this]
    (->> batch-queue
         ;; naive filter
         (filter (partial get cache))
         (batch-fn)
         (swap! cache merge))
    @cache))

(defn loader
  ([batch-fn]
   (AsyncLoader. (atom {}) (atom '()) batch-fn)))

(defn load [iloader key]
  (-batch iloader key)
  ;; just immediately run for now
  (-run iloader))

;; desired API
(comment
  (require '[dataloader.core :as data])
  (defn user-batch-fn [user-ids]
    ;; fetch user data based on IDs
    )
  (def user-loader (data/loader user-batch-fn))

  (data/load user-loader 12345)
  )
