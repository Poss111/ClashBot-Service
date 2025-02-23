# output "kinesis_arn" {
#   value       = aws_kinesis_stream.kinesis_stream.arn
#   description = "The ARN of the Kinesis Stream"
# }

output "ecs_task_execution_role_arn" {
  value       = aws_iam_role.ecs_task_execution_role.arn
  description = "The ARN of the ECS Task Execution Role"
}

output "ecs_task_role_arn" {
  value       = aws_iam_role.ecs_task_role.arn
  description = "The ARN of the ECS Task Role"
}

output "ecs_service_security_group_id" {
  value       = aws_security_group.ecs_task_security_group.id
  description = "The ID of the ECS Service Security Group"
}

output "load_balancer_dns" {
  value       = aws_lb.clash_bot_lb.dns_name
  description = "The DNS name of the Load Balancer"
}

output "load_balancer_arn" {
  value       = aws_lb.clash_bot_lb.arn
  description = "The ARN of the Load Balancer"
}

output "load_balancer_type" {
  value       = aws_lb.clash_bot_lb.load_balancer_type
  description = "The type of the Load Balancer"
}

output "load_balencer_name" {
  value       = aws_lb.clash_bot_lb.name
  description = "The name of the Load Balancer"
}
