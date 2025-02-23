output "api_gateway_url" {
  value       = aws_apigatewayv2_api.ecs_api.api_endpoint
  description = "API Gateway URL"
}
