resource "kubernetes_persistent_volume" "zookeeper" {
  metadata {
    name = "zookeeper-pv"
  }
  spec {
    capacity = {
      storage = "1Gi"
    }
    access_modes = ["ReadWriteOnce"]
    persistent_volume_source {
      host_path {
        path = "/mnt/data/zookeeper"
      }
    }
    storage_class_name = "manual"
  }
}

resource "kubernetes_persistent_volume_claim" "zookeeper" {
  metadata {
    name = "zookeeper-pvc"
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