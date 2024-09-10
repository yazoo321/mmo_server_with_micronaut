resource "kubernetes_namespace" "default" {
  metadata {
    name = "default"
  }
}

resource "kubernetes_deployment" "mongo" {
  metadata {
    name      = "mongo"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "mongo"
      }
    }
    template {
      metadata {
        labels = {
          app = "mongo"
        }
      }
      spec {
        container {
          name  = "mongo"
          image = "mongo:latest"

          port {
            container_port = 27017
          }

           env {
                  name  = "MONGO_INITDB_ROOT_USERNAME"
                  value = "mongo_mmo_server"
                }

                env {
                  name  = "MONGO_INITDB_ROOT_PASSWORD"
                  value = "mongo_password"
                }
        }
      }
    }
  }
}


resource "kubernetes_deployment" "redis" {
  metadata {
    name      = "redis"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "redis"
      }
    }
    template {
      metadata {
        labels = {
          app = "redis"
        }
      }
      spec {
        container {
          name  = "redis"
          image = "redis:latest"

          port {
            container_port = 6379
          }

          command = ["redis-server", "--save", "20", "1", "--loglevel", "warning"]

          volume_mount {
            name       = "redis-data"
            mount_path = "/data"
          }
        }

        volume {
          name = "redis-data"

          empty_dir {}
        }
      }
    }
  }
}

resource "kubernetes_service" "redis" {
  metadata {
    name      = "redis"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    selector = {
      app = "redis"
    }

    port {
      port        = 6379
      target_port = 6379
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_deployment" "zookeeper" {
  metadata {
    name      = "zookeeper"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "zookeeper"
      }
    }
    template {
      metadata {
        labels = {
          app = "zookeeper"
        }
      }
      spec {
        container {
          name  = "zookeeper"
          image = "confluentinc/cp-zookeeper:latest"

          port {
            container_port = 2181
          }

          env {
            name  = "ZOOKEEPER_CLIENT_PORT"
            value = "2181"
          }

          env {
            name  = "ZOOKEEPER_TICK_TIME"
            value = "2000"
          }

          volume_mount {
            name       = "zookeeper-data"
            mount_path = "/var/lib/zookeeper"
          }
        }

        volume {
          name = "zookeeper-data"

          empty_dir {}
        }
      }
    }
  }
}

resource "kubernetes_service" "zookeeper" {
  metadata {
    name      = "zookeeper"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    selector = {
      app = "zookeeper"
    }

    port {
      port        = 2181
      target_port = 2181
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_deployment" "kafka" {
  metadata {
    name      = "kafka"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "kafka"
      }
    }
    template {
      metadata {
        labels = {
          app = "kafka"
        }
      }
      spec {
        container {
          name  = "kafka"
          image = "confluentinc/cp-kafka:latest"

          port {
            container_port = 9092
          }

          port {
            container_port = 9093
          }

          env {
            name  = "KAFKA_BROKER_ID"
            value = "1"
          }

          env {
            name  = "KAFKA_ZOOKEEPER_CONNECT"
            value = "zookeeper:2181"
          }

          env {
            name  = "KAFKA_ADVERTISED_LISTENERS"
            value = "PLAINTEXT://localhost:9092,SASL_PLAINTEXT://localhost:9093"
          }

          env {
            name  = "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP"
            value = "PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT"
          }

          env {
            name  = "KAFKA_SECURITY_INTER_BROKER_PROTOCOL"
            value = "SASL_PLAINTEXT"
          }

          env {
            name  = "KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL"
            value = "PLAIN"
          }

          env {
            name  = "KAFKA_OPTS"
            value = "-Djava.security.auth.login.config=/etc/kafka/configs/kafka_server_jaas.conf"
          }

          env {
            name  = "KAFKA_AUTO_CREATE_TOPICS_ENABLE"
            value = "true"
          }

          volume_mount {
            name       = "kafka-config"
            mount_path = "/etc/kafka/configs"
          }
        }

        volume {
          name = "kafka-config"

          config_map {
            name = "kafka-config"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "kafka" {
  metadata {
    name      = "kafka"
    namespace = kubernetes_namespace.default.metadata[0].name
  }
  spec {
    selector = {
      app = "kafka"
    }

    port {
      port        = 9092
      target_port = 9092
    }

    port {
      port        = 9093
      target_port = 9093
    }

    type = "ClusterIP"
  }
}
