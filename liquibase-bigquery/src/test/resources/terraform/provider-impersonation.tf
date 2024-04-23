provider "google" {
  impersonate_service_account = "${var.deployer_sa}@${var.project_id}.iam.gserviceaccount.com"
}

provider "google-beta" {
  impersonate_service_account = "${var.deployer_sa}@${var.project_id}.iam.gserviceaccount.com"
}

