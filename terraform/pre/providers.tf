terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~>5.87.0"
    }
  }
  required_version = "~>1.9.0"
  backend "http" {
    address        = "https://app.harness.io/gateway/iacm/api/orgs/default/projects/ClashBot/workspaces/clashbotpre/terraform-backend?accountIdentifier=JxHtiRXHRZe2ZxkKlrnBqQ"
    username       = "harness"
    lock_address   = "https://app.harness.io/gateway/iacm/api/orgs/default/projects/ClashBot/workspaces/clashbotpre/terraform-backend/lock?accountIdentifier=JxHtiRXHRZe2ZxkKlrnBqQ"
    lock_method    = "POST"
    unlock_address = "https://app.harness.io/gateway/iacm/api/orgs/default/projects/ClashBot/workspaces/clashbotpre/terraform-backend/lock?accountIdentifier=JxHtiRXHRZe2ZxkKlrnBqQ"
    unlock_method  = "DELETE"
  }
}

provider "aws" {
  region = var.aws_region

  profile = "Master"

  default_tags {
    tags = var.tags
  }
}
