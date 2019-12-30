(ns pcap-common.macros)

(defmacro with-field-new [rconsumer field-name index & body]
  `(doto ~rconsumer
     (.startField ~field-name ~index)
     ~@body
     (.endField ~field-name ~index)))

(defmacro with-empty-field [rconsumer field-name index]
  `(doto ~rconsumer
     (.startField ~field-name ~index)
     (.endField ~field-name ~index)))

;; Java interface definition
(defn ^java.util.function.Function as-function [f]
  (reify java.util.function.Function
    (apply [this arg] (f arg))))

(defmacro jfn [& args]
  `(as-function (fn ~@args)))

(defn ^java.util.function.Consumer as-consumer [f]
  (reify java.util.function.Consumer
    (accept [this arg] (f arg))))

(defmacro jconsumer [& args]
  `(as-consumer (fn ~@args)))

;; becomes a macro, stays in CLJ
(defmacro build-optional [templateID & body]
  `(when (not (contains? ~'message-type-exclusions ~templateID)) ~@body))

(defmacro writeout [writer decoder]
  `(locking ~writer (.write ^ParquetRecordWriter ~writer nil (OutputRecord. ~'packet-usec ~'market-usec (~decoder ~'subOffset ~'actingBlockLength ~'actingVersion ~'buffer)))))

