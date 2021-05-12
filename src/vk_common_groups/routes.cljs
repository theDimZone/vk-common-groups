(ns vk-common-groups.routes
  (:require [vk-common-groups.views :as views]
            [vk-common-groups.events :as events]
            [re-frame.core :as re-frame]
            [reitit.frontend :as rf]
            [reitit.coercion.spec :as rss]))

(def routes
  [
   "/"
   [""
    {:name      ::main
     :menu-include? true
     :view      views/main-panel
     :link-text "Main"
     :controllers
     [{:start (fn [_] (js/console.log "Entering main"))
       :stop  (fn [_] (js/console.log "Leaving main"))}]}]
   ["auth"
    {:name      ::auth
     :menu-include? false
     :view      views/auth-panel
     :link-text "Auth"
     :controllers 
     [{:start (fn [_] (re-frame/dispatch [::events/new-auth]))
       :stop  (fn [_] (js/console.log "Leaving auth"))}]}]
   ["about"
    {:name      ::about
     :menu-include? true
     :view      views/about-panel
     :link-text "About"
     :controllers
     [{:start (fn [_] (js/console.log "Entering about"))
       :stop  (fn [_] (js/console.log "Leaving about"))}]}]
   ])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))