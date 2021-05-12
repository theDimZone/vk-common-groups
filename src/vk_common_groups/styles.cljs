(ns vk-common-groups.styles
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :refer [rgba]]))

(defglobal defaults
  [:body
   {}])

(defclass myfooter
  []
  {:position "absolute"
   :text-align "center"
   :width "100%"
   :padding "10px"
   :bottom "0px"})