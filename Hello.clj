(ns Hello
    (:gen-class :methods [[hello [Object] Object]]
                :main false))

(defn -hello [self x]
    (println (class x)) 
    #{"some" "data" "is available"})