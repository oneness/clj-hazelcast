(ns clj-hazelcast.test.core
  (:use [clojure.test])
  (:require
   [clj-hazelcast.core :as hazelcast]))

(def test-map (atom nil))

(defn fixture [f]
  (hazelcast/init)
  (reset! test-map (hazelcast/get-map "clj-hazelcast.cluster-tests.test-map"))
  (f))

(use-fixtures :once fixture)

(deftest map-test
  (is (= 1
         (do
           (hazelcast/put! @test-map :foo 1)
           (:foo @test-map))))
  (is 
   (let [m {:a [1 "two"] :b #{:three 'four}}]
     (= m
        (do
          (hazelcast/put! @test-map :bar m)
          (:bar @test-map))))))

(deftest listeners
  (is (= 2
         (let [events (atom [])
               listener-fn (fn [& event]
                             (swap! events conj event))
               listener (hazelcast/add-entry-listener! @test-map listener-fn)
               result (do
                        (hazelcast/put! @test-map :baz "foobar")
                        (hazelcast/put! @test-map :foo "bizbang")
                        (Thread/sleep 5)
                        (count @events))]
           (hazelcast/remove-entry-listener! @test-map listener)
           result))))
