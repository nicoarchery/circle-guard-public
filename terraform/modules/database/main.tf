# database/main.tf

resource "kubernetes_namespace" "db" {
  metadata {
    name = "database"
  }
}

resource "kubernetes_secret" "db_auth" {
  metadata {
    name      = "db-auth"
    namespace = kubernetes_namespace.db.metadata[0].name
  }

  data = {
    username = var.admin_username
    password = var.admin_password
  }
}

resource "kubernetes_service" "postgresql" {
  metadata {
    name      = "postgresql"
    namespace = kubernetes_namespace.db.metadata[0].name
  }
  spec {
    selector = {
      app = "postgresql"
    }
    port {
      port        = 5432
      target_port = 5432
    }
    type = "ClusterIP"
  }
}

resource "kubernetes_stateful_set" "postgresql" {
  metadata {
    name      = "postgresql"
    namespace = kubernetes_namespace.db.metadata[0].name
  }
  spec {
    service_name = "postgresql"
    replicas     = 1
    selector {
      match_labels = {
        app = "postgresql"
      }
    }
    template {
      metadata {
        labels = {
          app = "postgresql"
        }
      }
      spec {
        container {
          name  = "postgresql"
          image = "postgres:13"
          port {
            container_port = 5432
          }
          env {
            name  = "POSTGRES_DB"
            value = var.database_name
          }
          env {
            name  = "POSTGRES_USER"
            value = var.admin_username
          }
          env {
            name  = "POSTGRES_PASSWORD"
            value = var.admin_password
          }
        }
      }
    }
  }
}

