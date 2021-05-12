(ns vk-common-groups.coeffects
  (:require
   [clojure.string :as str]
   [hodgepodge.core :refer [local-storage]]
   [re-frame.core :as re-frame]))

(defn parse-query
  [query]
  (->> (str/split (str/replace-first (str query) "#" "") "&")
       (map (fn [kv-str]
              (let [kv (str/split kv-str "=")]
                {(-> kv first keyword) (-> kv second str)})))
       (reduce #(merge %1 %2))))

(re-frame/reg-cofx
 :url-params
 (fn [coeffects _]
   (assoc coeffects :url-params (parse-query (.. js/window -location -hash)))))

(re-frame/reg-cofx
 :local-db
 (fn [coeffects _]
   (assoc coeffects :local-db (js->clj (.parse js/JSON (:db local-storage)) :keywordize-keys true))))

(re-frame/reg-cofx 
 :last-input-id
 (fn [coeffects _]
   (assoc coeffects :last-input-id (last (keys (-> coeffects :db :inputs))))))