data "aws_vpc" "vpc" {
  id = var.vpc_id
}

# Subnets
data "aws_subnet" "subnet" {
  vpc_id = data.aws_vpc.vpc.id
}

# API Gateway
resource "aws_apigatewayv2_api" "ecs_api" {
  name          = "ecs-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_vpc_link" "ecs_vpc_link" {
  name               = "ecs-vpc-link"
  security_group_ids = [aws_security_group.clash_bot_lb_security_group.id]
  subnet_ids         = [data.aws_subnet.subnet.id]
}

resource "aws_apigatewayv2_integration" "ecs_integration" {
  api_id                 = aws_apigatewayv2_api.ecs_api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "http://${aws_lb.clash_bot_lb.dns_name}" # Forward to ALB
  connection_type        = "VPC_LINK"
  connection_id          = aws_apigatewayv2_vpc_link.ecs_vpc_link.id
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "ecs_route" {
  api_id    = aws_apigatewayv2_api.ecs_api.id
  route_key = "ANY /clash-bot/api/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.ecs_integration.id}"
}

resource "aws_apigatewayv2_stage" "ecs_stage" {
  api_id      = aws_apigatewayv2_api.ecs_api.id
  name        = "dev"
  auto_deploy = true
}

# Target Groups for Clash Bot ECS Task
resource "aws_lb_target_group" "clash_bot_target_group" {
  name     = "clash-bot-target-group"
  port     = var.container_port
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.vpc.id
}

# Target Group Attachment for ECS Task
resource "aws_lb_target_group_attachment" "clash_bot_target_group_attachment" {
  target_group_arn = aws_lb_target_group.clash_bot_target_group.arn
  target_id        = data.aws_ecs_service.clash_bot_service.id
  port             = var.container_port
}

# Security Group for Load Balancer
resource "aws_security_group" "clash_bot_lb_security_group" {
  name        = "clash-bot-lb-security-group"
  description = "Clash Bot Load Balancer Security Group"
  vpc_id      = data.aws_vpc.vpc.id

  ingress {
    from_port = var.container_port
    to_port   = var.container_port
    protocol  = "tcp"
    cidr_blocks = [
      data.aws_subnet.subnet.cidr_block
    ]
  }
}

# Application Load Balancer
resource "aws_lb" "clash_bot_lb" {
  name               = "clash-bot-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.clash_bot_lb_security_group.id]
  subnets            = [data.aws_subnet.subnet.id]
}

# Listener for Load Balancer
resource "aws_lb_listener" "clash_bot_lb_listener" {
  load_balancer_arn = aws_lb.clash_bot_lb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.clash_bot_target_group.arn
  }
}

# ECS Cluster
data "aws_ecs_cluster" "cluster" {
  cluster_name = var.cluster_name
}

# ECS Service for Clash Bot
data "aws_ecs_service" "clash_bot_service" {
  cluster_arn  = data.aws_ecs_cluster.cluster.arn
  service_name = var.clash_bot_service_name
}


