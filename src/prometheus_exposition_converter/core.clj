(ns prometheus-exposition-converter.core
  (:import [org.hawkular.agent.prometheus.text TextPrometheusMetricsProcessor]
           [org.hawkular.agent.prometheus.walkers PrometheusMetricsWalker]
           [java.io InputStream]))

(defn process
  "Processes the provided prometheus exposition with the provided walker.
  
  Returns the walker.
  The result should be fetched from the walker implementation, i.e. (.getResult walker) if using the CljMapPrometheusMetricsWalker."
  [^InputStream input-stream ^PrometheusMetricsWalker walker]
  (let [processor (TextPrometheusMetricsProcessor. input-stream walker)]
    (.walk processor)
    walker))
