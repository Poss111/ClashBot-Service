# API Gateway
resource "aws_apigatewayv2_api" "ecs_api" {
  name          = "ecs-api"
  protocol_type = "HTTP"
}

data "aws_security_group" "clash_bot_lb_security_group" {
  name = "clash-bot-lb-security-group"
}

resource "aws_apigatewayv2_vpc_link" "ecs_vpc_link" {
  name               = "ecs-vpc-link"
  security_group_ids = [data.aws_security_group.clash_bot_lb_security_group.id]
  subnet_ids = [
    data.aws_subnet.subnet.id,
    data.aws_subnet.subnet-two.id
  ]
}

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

resource "aws_apigatewayv2_route" "ecs_route" {
  api_id    = aws_apigatewayv2_api.ecs_api.id
  route_key = "ANY ${var.path}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.ecs_integration.id}"
}

resource "aws_apigatewayv2_stage" "ecs_stage" {
  api_id      = aws_apigatewayv2_api.ecs_api.id
  name        = "$default"
  auto_deploy = true
}
