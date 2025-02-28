# API Gateway
resource "aws_apigatewayv2_api" "ecs_api" {
  name          = "clash-bot-services-apis"
  protocol_type = "HTTP"
  description   = "Clash Bot Services API"
  cors_configuration {
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_origins = ["*"]
  }

}

resource "aws_security_group" "vpc_link_security_group" {
  name        = "clash-bot-vpc_link_security_group"
  description = "Clash Bot VPC Link Security Group"
  vpc_id      = data.aws_vpc.vpc.id
}

resource "aws_vpc_security_group_ingress_rule" "http_vpc_link_security_ingress_rule" {
  security_group_id = aws_security_group.vpc_link_security_group.id
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_apigatewayv2_vpc_link" "ecs_vpc_link" {
  name = "ecs-vpc-link"
  security_group_ids = [
    aws_security_group.vpc_link_security_group.id
  ]
  subnet_ids = [
    data.aws_subnet.subnet.id,
    data.aws_subnet.subnet-two.id
  ]
}

# Listener for 80
data "aws_lb_listener" "clash_bot_lb_listener" {
  load_balancer_arn = var.load_balancer_arn
  port              = 80
}

resource "aws_apigatewayv2_integration" "ecs_integration" {
  api_id                 = aws_apigatewayv2_api.ecs_api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = data.aws_lb_listener.clash_bot_lb_listener.arn # Forward to ALB
  connection_type        = "VPC_LINK"
  connection_id          = aws_apigatewayv2_vpc_link.ecs_vpc_link.id
  payload_format_version = "1.0"
}

# Listener for 443
data "aws_lb_listener" "clash_bot_lb_listener_443" {
  load_balancer_arn = var.load_balancer_arn
  port              = 443
}

resource "aws_apigatewayv2_integration" "ecs_integration_443" {
  api_id                 = aws_apigatewayv2_api.ecs_api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = data.aws_lb_listener.clash_bot_lb_listener_443.arn # Forward to ALB
  connection_type        = "VPC_LINK"
  connection_id          = aws_apigatewayv2_vpc_link.ecs_vpc_link.id
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "ecs_route" {
  api_id    = aws_apigatewayv2_api.ecs_api.id
  route_key = "ANY ${var.path}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.ecs_integration.id}"
}

resource "aws_apigatewayv2_stage" "ecs_stage" {
  api_id      = aws_apigatewayv2_api.ecs_api.id
  name        = "$default"
  auto_deploy = true
  default_route_settings {
    detailed_metrics_enabled = true
    logging_level            = "INFO"
  }
}
