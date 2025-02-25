# Application Load Balancer
data "aws_subnet" "subnet" {
  vpc_id = data.aws_vpc.vpc.id
  id     = var.subnet_one_id
}

data "aws_subnet" "subnet-two" {
  vpc_id = data.aws_vpc.vpc.id
  id     = var.subnet_two_id
}

resource "aws_lb" "clash_bot_lb" {
  name               = "clash-bot-service-lb"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.clash_bot_lb_security_group.id]
  subnets = [
    data.aws_subnet.subnet.id,
    data.aws_subnet.subnet-two.id
  ]
}

resource "aws_lb_target_group" "ecs_tg" {
  name     = "clash-bot-ecs-target-group"
  port     = 80
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.vpc.id
  health_check {
    path                = "/actuator/health/readiness"
    protocol            = "HTTPS"
    port                = 8080
    interval            = 60
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 5
  }
  target_type = "ip"
}

# Security Group for Load Balancer
resource "aws_security_group" "clash_bot_lb_security_group" {
  name        = "clash-bot-lb-security-group"
  description = "Clash Bot Load Balancer Security Group"
  vpc_id      = data.aws_vpc.vpc.id
}

resource "aws_vpc_security_group_ingress_rule" "http_clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  from_port         = 80
  to_port           = var.container_port
  ip_protocol       = "tcp"
  cidr_ipv4         = data.aws_subnet.subnet.cidr_block
}

resource "aws_vpc_security_group_ingress_rule" "https_clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  from_port         = 443
  to_port           = var.container_port
  ip_protocol       = "tcp"
  cidr_ipv4         = data.aws_subnet.subnet.cidr_block
}

resource "aws_vpc_security_group_egress_rule" "clash_bot_lb_security_ingress_rule" {
  security_group_id = aws_security_group.clash_bot_lb_security_group.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# Listener for Load Balancer
resource "aws_lb_listener" "clash_bot_lb_listener" {
  load_balancer_arn = aws_lb.clash_bot_lb.arn
  port              = var.load_balancer_port
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.ecs_tg.arn
  }
}

data "aws_acm_certificate" "cert" {
  domain   = var.domain_name
  statuses = ["ISSUED"]
}

# Listener for Load Balancer HTTPS
resource "aws_lb_listener" "clash_bot_lb_https_listener" {
  load_balancer_arn = aws_lb.clash_bot_lb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = data.aws_acm_certificate.cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.ecs_tg.arn
  }
}

# ECS Cluster
data "aws_ecs_cluster" "cluster" {
  cluster_name = var.cluster_name
}
