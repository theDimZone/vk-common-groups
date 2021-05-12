(ns vk-common-groups.config
  (:import
   [goog.string Const]))

(def debug?
  ^boolean goog.DEBUG)

(def vk-auth-url 
  "https://oauth.vk.com/authorize?client_id=7846513&redirect_uri=http://127.0.0.1:8280/auth&response_type=token&scope=262146")

(def vk-api-url
  (goog.string.Const.from "https://api.vk.com/method/%{method}"))