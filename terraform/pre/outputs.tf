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
