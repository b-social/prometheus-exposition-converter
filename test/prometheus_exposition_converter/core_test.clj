(ns prometheus-exposition-converter.core-test
  (:require [clojure.test :refer :all]
            [prometheus-exposition-converter.core :refer [process]]
            [clojure.java.io :as io])
  (:import [prometheus-exposition-converter.walkers CljMapPrometheusMetricsWalker]))

(deftest test-process-prometheus-exposition-success
  (let [walker (CljMapPrometheusMetricsWalker.)
        exposition (io/input-stream "test_resources/exposition.txt")
        output (.getResult (process exposition walker))]
    (testing "the result is correct"
      (is (some? (:metrics output)))
      (is (some? (:processing-time output)))
      (is (= 2 (:families-processed output)))
      (is (= 5 (:metrics-processed output))))

    (let [metrics (:metrics output)
          http-request-latency-seconds (first metrics)
          http-request-latency-metric-first (first (:metrics http-request-latency-seconds))
          http-request-latency-metric-last (last (:metrics http-request-latency-seconds))
          http-requests-total (second metrics)
          http-requests-total-metric-first (first (:metrics http-requests-total))
          http-requests-total-metrics-last (last (:metrics http-requests-total))]
      (testing "the metrics is correct"
        (is (= 2 (count metrics))))

      (testing "the http request latency is correct"
        (is (= "http_request_latency_seconds" (:name http-request-latency-seconds)))
        (is (= "HTTP request latency" (:help http-request-latency-seconds)))
        (is (= "HISTOGRAM" (:type http-request-latency-seconds)))
        (is (= 2 (count (:metrics http-request-latency-seconds)))))

      (testing "the http request latency first metrics is correct"
        (is (= 0.655 (:sum http-request-latency-metric-first)))
        (is (= 6 (:count http-request-latency-metric-first)))
        (is (= 15 (count (:buckets http-request-latency-metric-first))))
        (is (= 2 (count (:labels http-request-latency-metric-first))))
        (is (= "open-banking-backend-iron.development-cobalt.b-anti-social.io"
              (get-in http-request-latency-metric-first [:labels "host"])))
        (is (= "" (get-in http-request-latency-metric-first [:labels "path"])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 2.0])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 4.0])))
        (is (= 1 (get-in http-request-latency-metric-first [:buckets 0.075])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 0.3])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 0.5])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 1.0])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 5.0])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 10.0])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 3.0])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 0.75])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 1.5])))
        (is (= 2 (get-in http-request-latency-metric-first [:buckets 0.1])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 0.4])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets 0.2])))
        (is (= 6 (get-in http-request-latency-metric-first [:buckets Double/POSITIVE_INFINITY]))))

      (testing "the http request latency last metrics is correct"
        (is (= 0.0 (:sum http-request-latency-metric-last)))
        (is (= 18 (:count http-request-latency-metric-last)))
        (is (= 20 (count (:buckets http-request-latency-metric-last))))
        (is (= 2 (count (:labels http-request-latency-metric-last))))
        (is (= "open-banking-backend-iron.development-cobalt.b-anti-social.io"
              (get-in http-request-latency-metric-last [:labels "host"])))
        (is (= "/" (get-in http-request-latency-metric-last [:labels "path"])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 2.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 4.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.075])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.3])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.5])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 1.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 5.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 10.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 3.0])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.75])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 1.5])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.1])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.05])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.4])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.2])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.03])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.01])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.02])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets 0.005])))
        (is (= 18 (get-in http-request-latency-metric-last [:buckets Double/POSITIVE_INFINITY]))))

      (testing "the http requests total is correct"
        (is (= "http_requests_total" (:name http-requests-total)))
        (is (= "Number of HTTP requests" (:help http-requests-total)))
        (is (= "COUNTER" (:type http-requests-total)))
        (is (= 3 (count (:metrics http-requests-total)))))

      (testing "the http requests total first is correct"
        (is (= 11.0 (:value http-requests-total-metric-first)))
        (is (= "open-banking-backend-iron.development-cobalt.b-anti-social.io"
              (get-in http-requests-total-metric-first [:labels "host"])))
        (is (= "/" (get-in http-requests-total-metric-first [:labels "path"])))
        (is (= "200" (get-in http-requests-total-metric-first [:labels "status"])))
        (is (= "2XX" (get-in http-requests-total-metric-first [:labels "statusClass"]))))

      (testing "the http requests total last is correct"
        (is (= 7.0 (:value http-requests-total-metrics-last)))
        (is (= "open-banking-backend-iron.development-cobalt.b-anti-social.io"
              (get-in http-requests-total-metrics-last [:labels "host"])))
        (is (= "/" (get-in http-requests-total-metrics-last [:labels "path"])))
        (is (= "400" (get-in http-requests-total-metrics-last [:labels "status"])))
        (is (= "4XX" (get-in http-requests-total-metrics-last [:labels "statusClass"])))))))

(deftest test-process-prometheus-exposition-bad-file
  (let [walker (CljMapPrometheusMetricsWalker.)
        exposition (io/input-stream "test_resources/bad.txt")
        output (.getResult (process exposition walker))]
    (testing "the result is correct"
      (is (some? (:metrics output)))
      (is (some? (:processing-time output)))
      (is (= 0 (:families-processed output)))
      (is (= 0 (:metrics-processed output))))))

