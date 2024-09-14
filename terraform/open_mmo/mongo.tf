resource "kubernetes_persistent_volume" "mongo" {
  metadata {
    name = "mongo-pv"
  }
  spec {
    capacity = {
      storage = "1Gi"
    }
    access_modes = ["ReadWriteOnce"]
    persistent_volume_source {
      host_path {
        path = "/mnt/data/mongo"
      }
    }
    storage_class_name = "manual"
  }
}

resource "kubernetes_persistent_volume_claim" "mongo" {
  metadata {
    name = "mongo-pvc"
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

resource "kubernetes_service" "mongo" {
  metadata {
    name = "mongo-service"
    namespace = kubernetes_namespace.main.metadata[0].name
  }
  spec {
    selector = {
      app = "mongo"
    }
    port {
      port          = 27017
      target_port   = 27017
    }
    type = "ClusterIP"
  }
}