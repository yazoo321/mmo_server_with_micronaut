resource "kubernetes_namespace" "main" {
  metadata {
    name = "main"
  }
}

resource "kubernetes_deployment" "mongo" {
  metadata {
    name      = "mongo"
    namespace = kubernetes_namespace.main.metadata[0].name
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
    namespace = kubernetes_namespace.main.metadata[0].name
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
    namespace = kubernetes_namespace.main.metadata[0].name
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
    namespace = kubernetes_namespace.main.metadata[0].name
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
    namespace = kubernetes_namespace.main.metadata[0].name
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
    namespace = kubernetes_namespace.main.metadata[0].name
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
          name  = "kafka-broker"
          image = "confluentinc/cp-kafka:latest"

          env {
            name  = "KAFKA_BROKER_ID"
            value = "1"
          }
          env {
            name  = "KAFKA_ZOOKEEPER_CONNECT"
            value = "zookeeper:2181"
          }
#          env {
#            name  = "ZOOKEEPER_SASL_ENABLED"
#            value = "false"
#          }
#          env {
#            name  = "KAFKA_ADVERTISED_LISTENERS"
#            value = "PLAINTEXT://kafka-broker:9092,SASL_PLAINTEXT://kafka-broker:9093"
#          }
          env {
            name  = "KAFKA_ADVERTISED_LISTENERS"
            value = "PLAINTEXT://kafka-broker:9092"
          }
#          env {
#            name  = "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP"
#            value = "PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT"
#          }
          env {
            name  = "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP"
            value = "PLAINTEXT:PLAINTEXT"
          }
          env {
            name  = "KAFKA_SECURITY_INTER_BROKER_PROTOCOL"
            value = "PLAINTEXT"
          }
          env {
            name  = "KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL"
            value = "PLAIN"
          }
#          env {
#            name  = "KAFKA_OPTS"
#            value = "-Djava.security.auth.login.config=/etc/kafka/configs/kafka_server_jaas.conf"
#          }
          env {
            name  = "KAFKA_AUTO_CREATE_TOPICS_ENABLE"
            value = "true"
          }
          env {
            name  = "KAFKA_SASL_ENABLED_MECHANISMS"
            value = "PLAIN"
          }
          env {
            name  = "KAFKA_DEFAULT_REPLICATION_FACTOR"
            value = "1"
          }
          env {
            name  = "KAFKA_LOG_RETENTION_HOURS"
            value = "1"
          }
          env {
            name  = "KAFKA_NUM_PARTITIONS"
            value = "2"
          }
          env {
            name  = "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR"
            value = "1"
          }

#          volume_mount {
#            name       = "kafka-config"
#            mount_path = "/etc/kafka/configs/kafka_server_jaas.conf"
#            sub_path   = "kafka_server_jaas_conf"
#          }

          volume_mount {
            name       = "kafka-config"
            mount_path = "/etc/kafka/configs/config.properties"
            sub_path   = "server_properties"
          }
        }

        volume {
          name = "kafka-config"
          config_map {
            name = kubernetes_config_map.kafka_config.metadata[0].name
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "kafka" {
  metadata {
    name      = "kafka-broker"
    namespace = kubernetes_namespace.main.metadata[0].name
  }
  spec {
    selector = {
      app = "kafka"
    }
    port {
      name = "plaintext"
      port        = 9092
      target_port = 9092
    }
    port {
      name = "sasl-plaintext"
      port        = 9093
      target_port = 9093
    }
    type = "ClusterIP"
  }
}

resource "kubernetes_deployment" "micronaut_vm" {
  metadata {
    name = "mmo-server"
    namespace = kubernetes_namespace.main.metadata[0].name

    labels = {
      app = "mmo-server"
    }
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "mmo-server"
      }
    }
    template {
      metadata {
        labels = {
          app = "mmo-server"
        }
      }
      spec {
        container {
          name  = "mmo-server"
          image = "openmmoregistry.azurecr.io/myapp/mmo-server:latest"
          image_pull_policy = "Always"
          port {
            container_port = 8081
          }
        }
      }
    }
  }
}