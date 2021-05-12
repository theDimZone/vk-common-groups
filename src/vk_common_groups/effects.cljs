(ns vk-common-groups.effects
  (:require [re-frame.core :as re-frame]
            [re-frame.db :as rdb]
            [vk-common-groups.routes :as routes]
            [vk-common-groups.events :as events]
            [vk-common-groups.config :as config]
            [reitit.core :as r]
            [reitit.frontend.easy :as rfe]
            [hodgepodge.core :refer [local-storage clear!]])
  (:import [goog.net Jsonp]
           [goog Uri]
           [goog.html TrustedResourceUrl]))

(re-frame/reg-fx
 :push-state
 (fn [route]
   (apply rfe/push-state route)))

(re-frame/reg-fx
 :redirect
 (fn [path]
   ;(let [matched (r/match-by-path routes/router path)]
     (set! (.. js/window -location -hash) "")
     (set! (.. js/window -location -pathname) path)))
;(re-frame/dispatch [::events/navigated matched])

(re-frame/reg-fx
 :local-save
 (fn [db]
   (clear! local-storage)
   (assoc! local-storage :db (->> db
                                  (filter #(not (= (first %) :current-route)))
                                  (into {})
                                  (clj->js)
                                  (.stringify js/JSON)))))

(defn error-handler [res]
  (.log js/console (str "Requst error: " res)))

(re-frame/reg-fx
 :jsonp-vk-request
 (fn [{:keys [method params handler]}]
   (let [url (goog.html.TrustedResourceUrl.formatWithParams config/vk-api-url #js{"method" method} (clj->js params))
         jsonp (goog.net.Jsonp. url "callback")]
     (.send jsonp
            nil
            #(re-frame/dispatch [handler (merge {:params params} (js->clj % :keywordize-keys true))])
            error-handler))))