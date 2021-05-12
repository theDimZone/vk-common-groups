(ns vk-common-groups.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [vk-common-groups.events :as events]
   [vk-common-groups.routes :as routes]
   [vk-common-groups.views :as views]
   [vk-common-groups.config :as config]
   [vk-common-groups.effects :as effects]
   [reitit.frontend :as rf]
   [reitit.coercion.spec :as rss]
   [reitit.frontend.easy :as rfe]
   ))


(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [::events/navigated new-match])))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/wrapper-panel {:router routes/router}] root-el)))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   routes/router
   on-navigate
   {:use-fragment false}))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch [::events/restore-local-state])
  (dev-setup)
  (init-routes!)
  (mount-root))


