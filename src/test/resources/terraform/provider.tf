terraform {
  required_version = ">= 1.1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 3.0.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = ">= 4.0.0"
    }
    github = {
      source  = "integrations/github"
      version = "~> 4.0"
    }
    spacelift = {
          source = "spacelift-io/spacelift"
        }
  }
}

provider "google" {
  alias       = "impersonate_service_account"
  project     = "testharnesstests"
  region      = "us-east1"
  impersonate_service_account = "gh-bq-deployer@testharnesstests.iam.gserviceaccount.com"
}


provider "spacelift" {}