(ns prometheus-exposition-converter.walkers.map
  "Provides an implementation of org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker
  that parses a prometheus exposition and stores it, retrievable under (.getResult)"
  (:import [org.hawkular.agent.prometheus.types MetricFamily Counter Gauge
                                                Summary Summary$Quantile
                                                Histogram Histogram$Bucket]
           [java.time Instant]))


(gen-class
  :name prometheus-exposition-converter.walkers.CljMapPrometheusMetricsWalker
  :implements [org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker]
  :state state
  :init init
  :prefix "-"
  :main false
  :methods [[getResult [] clojure.lang.IPersistentMap]])

(defn- map-quantile
  [val ^Summary$Quantile quantile]
  (assoc val (.getQuantile quantile) (.getValue quantile)))

(defn- map-buckets
  [val ^Histogram$Bucket bucket]
  (assoc val (.getUpperBound bucket) (.getCumulativeCount bucket)))

(defn -init []
  "Called when a walk has been started.

  Init function for CljMapPrometheusMetricsWalker"
  [[] (atom {})])

(defn -walkStart
  "void walkStart()

  Called when a walk has been started."
  [this]
  (reset! (.state this) {:metrics            []
                         :current            nil
                         :processing-time    (.toEpochMilli (Instant/now))
                         :families-processed 0
                         :metrics-processed  0}))

(defn -walkFinish
  "void walkFinish(int familiesProcessed, int metricsProcessed)

  Called when a walk has traversed all the metrics."
  [this families-processed metrics-processed]
  (let [state (.state this)
        current (:current @state)]
    (do
      (if (some? current)
        (swap! state update-in [:metrics] conj @current))
      (swap! state assoc
        :families-processed families-processed
        :metrics-processed metrics-processed))))

(defn -walkMetricFamily
  "void walkMetricFamily(MetricFamily family, int index)

  Called when a new metric family is about to be traversed."
  [this ^MetricFamily family index]
  (let [state (.state this)
        name (.getName family)
        help (.getHelp family)
        type (.toString (.getType family))
        next (atom {:name    name
                    :help    help
                    :type    type
                    :metrics []})
        current (:current @state)]
    (do
      (if (some? current)
        (swap! state update-in [:metrics] conj @current))
      (swap! state assoc :current next))))

(defn -walkCounterMetric
  "void walkCounterMetric(MetricFamily family, Counter counter, int index)

  Called when a new counter metric is found."
  [this ^MetricFamily family ^Counter counter index]
  (let [state (.state this)
        current (:current @state)
        value (.getValue counter)
        labels (.getLabels counter)
        metric {:value  value
                :labels (into {} labels)}]
    (swap! current update-in [:metrics] conj metric)))

(defn -walkGaugeMetric
  "void walkGaugeMetric(MetricFamily family, Gauge gauge, int index)

  Called when a new gauge metric is found."
  [this ^MetricFamily family ^Gauge gauge index]
  (let [state (.state this)
        current (:current @state)
        value (.getValue gauge)
        labels (.getLabels gauge)
        metric {:value  value
                :labels (into {} labels)}]
    (swap! current update-in [:metrics] conj metric)))

(defn -walkSummaryMetric
  "void walkSummaryMetric(MetricFamily family, Summary summary, int index)

  Called when a new summary metric is found."
  [this ^MetricFamily family ^Summary summary index]
  (let [state (.state this)
        current (:current @state)
        sum (.getSampleSum summary)
        count (.getSampleCount summary)
        labels (.getLabels summary)
        quantiles (.getQuantiles summary)
        metric {:sum       sum
                :count     count
                :quantiles (reduce map-quantile {} quantiles)
                :labels    (into {} labels)}]
    (swap! current update-in [:metrics] conj metric)))

(defn -walkHistogramMetric
  "void walkHistogramMetric(MetricFamily family, Histogram histogram, int index)

  Called when a new histogram metric is found."
  [this ^MetricFamily family ^Histogram histogram index]
  (let [state (.state this)
        current (:current @state)
        sum (.getSampleSum histogram)
        count (.getSampleCount histogram)
        labels (.getLabels histogram)
        buckets (.getBuckets histogram)
        metric {:sum     sum
                :count   count
                :buckets (reduce map-buckets {} buckets)
                :labels  (into {} labels)}]
    (swap! current update-in [:metrics] conj metric)))

(defn -getResult
  [this]
  (let [state (.state this)]
    (dissoc @state :current)))


