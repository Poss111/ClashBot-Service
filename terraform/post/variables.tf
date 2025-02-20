variable "aws_region" {
  description = "AWS region"
  default     = "us-east-1"
}

variable "container_port" {
  description = "Container port"
  default     = 80
}

variable "cluster_name" {
  description = "ECS Cluster Name"
  default     = "clash-bot-cluster"
}

variable "clash_bot_service_name" {
  description = "Clash Bot Service Name"
  default     = "clash-bot-service"
}

variable "vpc_id" {
  description = "VPC ID"
}
