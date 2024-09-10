 resource "kubernetes_persistent_volume" "kafka" {
   metadata {
     name = "kafka-pv"
   }
   spec {
     capacity {
       storage = "1Gi"
     }
     access_modes = ["ReadWriteOnce"]
     host_path {
       path = "/mnt/data/kafka"
     }
     storage_class_name = "manual"
   }
 }

 resource "kubernetes_persistent_volume_claim" "kafka" {
   metadata {
     name = "kafka-pvc"
   }
   spec {
     access_modes = ["ReadWriteOnce"]
     resources {
       requests {
         storage = "1Gi"
       }
     }
     storage_class_name = "manual"
   }
 }