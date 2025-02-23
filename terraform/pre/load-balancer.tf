# Application Load Balancer
locals {
  host_network = "0.0.0.0/0"
}

resource "aws_lb" "clash_bot_lb" {
  name               = "clash-bot-service-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.clash_bot_lb_security_group.id]
  subnets = [
    data.aws_subnet.subnet.id,
    data.aws_subnet.subnet-two.id
  ]
}

# Security Group for Load Balancer
resource "aws_security_group" "clash_bot_lb_security_group" {
  name        = "clash-bot-lb-security-group"
  description = "Clash Bot Load Balancer Security Group"
  vpc_id      = data.aws_vpc.vpc.id
}

resource "aws_vpc_security_group_ingress_rule" "clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  from_port         = 80
  to_port           = var.container_port
  ip_protocol       = "tcp"
  cidr_ipv4 = [
    data.aws_subnet.subnet.cidr_block,
    data.aws_subnet.subnet-two.cidr_block
  ]
}

resource "aws_vpc_security_group_ingress_rule" "clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  from_port         = 443
  to_port           = var.container_port
  ip_protocol       = "tcp"
  cidr_ipv4 = [
    data.aws_subnet.subnet.cidr_block,
    data.aws_subnet.subnet-two.cidr_block
  ]
}

resource "aws_vpc_security_group_egress_rule" "clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  ip_protocol       = "-1"
  cidr_ipv4 = [
    local.host_network
  ]
}

# Listener for Load Balancer
resource "aws_lb_listener" "clash_bot_lb_listener" {
  load_balancer_arn = aws_lb.clash_bot_lb.arn
  port              = var.load_balancer_port
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/html"
      message_body = "<html><body><h1>Maintenance Mode</h1><p>How did you make it here! We'll be back soon.</p></body></html>"
      status_code  = "503"
    }
  }
}

# ECS Cluster
data "aws_ecs_cluster" "cluster" {
  cluster_name = var.cluster_name
}
