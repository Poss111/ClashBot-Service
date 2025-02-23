variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "tags" {
  description = "Tags"
  type        = map(string)
  default = {
    Name = "terraform-pre"
  }
}

variable "cluster_name" {
  description = "ECS cluster name"
  type        = string
}

variable "container_port" {
  description = "Container port"
  type        = number
}

variable "load_balancer_port" {
  description = "Load balancer port"
  type        = number
}

variable "subnet_one_id" {
  description = "Subnet one ID"
  type        = string
}

variable "subnet_two_id" {
  description = "Subnet two ID"
  type        = string
}
