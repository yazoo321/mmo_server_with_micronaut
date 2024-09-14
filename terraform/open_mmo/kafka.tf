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
     kafka_server_jaas_conf = <<-EOT
      KafkaServer {
        org.apache.kafka.common.security.plain.PlainLoginModule required
        username="kafka"
        password="password123";
      };
      Client {
        org.apache.zookeeper.server.auth.DigestLoginModule required
        username="kafka"
        password="password123";
      };
    EOT
   }
 }

