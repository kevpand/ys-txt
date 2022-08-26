(ns ys-txt.main
  (:require [clojure.java.io :as io]
            [tick.core :as t]
            [twttr.api]
            [twttr.auth]
            [clojure.string :as str]))

(def ^:private twitter-creds
  (twttr.auth/env->UserCredentials))

(def ^:private lyric-files
  (->> (-> (io/resource "lyrics")
           io/file
           file-seq)
       (filter #(.isFile %))))

(def ^:private extract-lyrics
  (comp
   (map slurp)
   (mapcat #(str/split % #"\n"))
   (filter #(not= "" %))))

(defn- make-lyrics []
  (shuffle (sequence extract-lyrics lyric-files)))

(def ^:private initial-hours
  "UTC hours we'd like to send a status in, targetting throughout the
  day in Chicago timezone"
  [14 17 20 23 2])

(defn- post-tweet [tweet]
  (twttr.api/statuses-update twitter-creds
                             :params {:status tweet}))

(defn main []
  (loop [hours initial-hours
         minute (rand-int 60)
         curr-lyrics (make-lyrics)]
    (let [now (t/now)
          now-hour (t/hour now)
          now-minute (t/minute now)
          hour (first hours)]
      (cond
        (nil? hour) (recur
                     initial-hours
                     minute
                     curr-lyrics)
        (not (seq curr-lyrics)) (recur
                                 hours
                                 minute
                                 (make-lyrics))
        :else (if (and (= now-minute minute)
                       (= now-hour hour))
                (let [tweet (first curr-lyrics)]
                  (post-tweet tweet)
                  (recur (rest hours)
                         (rand-int 60)
                         (rest curr-lyrics)))
                (recur hours
                       minute
                       curr-lyrics))))))
