variable "aws_region" {
  description = "AWS region"
  default     = "us-east-1"
}

variable "vpc_id" {
  description = "VPC ID"
}

variable "tags" {
  description = "Tags"
  type        = map(string)
  default = {
    Name = "terraform-pre"
  }
}
