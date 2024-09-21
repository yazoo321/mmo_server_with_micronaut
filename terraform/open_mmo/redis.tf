resource "kubernetes_persistent_volume" "redis" {
  metadata {
    name = "redis-pv"
  }
  spec {
    capacity = {
      storage = "1Gi"
    }
    access_modes = ["ReadWriteOnce"]
        persistent_volume_source {
          host_path {
            path = "/mnt/data/redis"
          }
        }
    storage_class_name = "manual"
  }
}

resource "kubernetes_persistent_volume_claim" "redis" {
  metadata {
    name = "redis-pvc"
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