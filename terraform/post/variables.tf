variable "aws_region" {
  description = "AWS region"
  default     = "us-east-1"
  type        = string
}

variable "container_port" {
  description = "Container port"
  default     = 80
  type        = number
}

variable "cluster_name" {
  description = "ECS Cluster Name"
  default     = "clash-bot-cluster"
  type        = string
}

variable "clash_bot_service_name" {
  description = "Clash Bot Service Name"
  default     = "clash-bot-service"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_one_id" {
  description = "Subnet One ID"
  type        = string
}

variable "subnet_two_id" {
  description = "Subnet Two ID"
  type        = string
}

variable "load_balancer_arn" {
  description = "Load Balancer ARN"
  type        = string
}

variable "path" {
  description = "Path"
  default     = "/clash-bot"
  type        = string
}
