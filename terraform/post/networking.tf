data "aws_vpc" "vpc" {
  id = var.vpc_id
}

# Subnets
data "aws_subnet" "subnet" {
  vpc_id = data.aws_vpc.vpc.id
  id     = "subnet-092e50e03e16be078"
}

# Subnets
data "aws_subnet" "subnet-two" {
  vpc_id = data.aws_vpc.vpc.id
  id     = "subnet-02d5547ced2a60b1d"
}

# Target Groups for Clash Bot ECS Task
resource "aws_lb_target_group" "clash_bot_target_group" {
  name     = "clash-bot-target-group"
  port     = var.container_port
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.vpc.id
}

