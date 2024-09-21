 resource "kubernetes_persistent_volume" "kafka" {
   metadata {
     name = "kafka-pv"
   }
   spec {
     capacity = {
       storage = "1Gi"
     }
     access_modes = ["ReadWriteOnce"]
     persistent_volume_source {
       host_path {
         path = "/mnt/data/kafka"
       }
     }
     storage_class_name = "manual"
   }
 }

 resource "kubernetes_persistent_volume_claim" "kafka" {
   metadata {
     name = "kafka-pvc"
     namespace = kubernetes_namespace.main.metadata[0].name
   }
   spec {
     access_modes = ["ReadWriteOnce"]
     resources {
       requests = {
         storage = "1Gi"
       }
     }
     storage_class_name = "manual"
   }
 }
 resource "kubernetes_config_map" "kafka_config" {
   metadata {
     name      = "kafka-config"
     namespace = kubernetes_namespace.main.metadata[0].name
   }

   data = {
#     kafka_server_jaas_conf = <<-EOT
#      KafkaServer {
#        org.apache.kafka.common.security.plain.PlainLoginModule required
#        username="kafka"
#        password="password123";
#      };
#    EOT

#    server_properties = <<-EOT
#      # Define listeners
#      listeners=SASL_PLAINTEXT://0.0.0.0:9093
#      advertised.listeners=SASL_PLAINTEXT://kafka-broker:9093
#
#      # Enable SASL_PLAINTEXT security protocol
#      security.protocol=SASL_PLAINTEXT
#      sasl.mechanism=PLAIN
#
#      # Define JAAS configuration for SASL
#      sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka" password="password123";
#
#      # Logging configuration
#      log.dirs=/var/lib/kafka/data
#    EOT
    server_properties = <<-EOT
      # Define listeners
      listeners=PLAINTEXT://0.0.0.0:9092
      advertised.listeners=PLAINTEXT://kafka-broker:9092

      # Enable PLAINTEXT security protocol (no SASL)
      security.protocol=PLAINTEXT

      # Logging configuration
      log.dirs=/var/lib/kafka/data
    EOT
   }
 }

