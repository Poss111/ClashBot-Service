# Kinesis
# resource "aws_kinesis_stream" "kinesis_stream" {
#   name             = "kinesis-stream"
#   shard_count      = 1
#   retention_period = 24
#   stream_mode_details {
#     stream_mode = "ON_DEMAND"
#   }
# }

locals {
  prefix = "clash-bot-service"
}

# An Task Execution Role for ECS
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${local.prefix}-ecs-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

# Policy attachment for ECS Task Execution Role
resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.ecs_task_execution_role_policy.arn
}

# An Task Execution Role Policy for ECS
resource "aws_iam_policy" "ecs_task_execution_role_policy" {
  name = "${local.prefix}-ecs-task-execution-role-policy"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Action" : [
            "ecr:GetAuthorizationToken",
            "ecr:BatchCheckLayerAvailability",
            "ecr:GetDownloadUrlForLayer",
            "ecr:BatchGetImage",
            "logs:CreateLogGroup",
            "logs:CreateLogStream",
            "logs:PutLogEvents"
          ],
          "Effect" : "Allow",
          "Resource" : "*"
        },
        {
          "Action" : "secretsmanager:GetSecretValue",
          "Effect" : "Allow",
          "Resource" : "arn:aws:secretsmanager:*:*:secret:prod/MrBot/*"
        },
        {
          "Action" : "secretsmanager:GetSecretValue",
          "Effect" : "Allow",
          "Resource" : "arn:aws:secretsmanager:*:*:secret:RIOT_TOKEN*"
        }
      ]
  })
}

# An Task Role for ECS
resource "aws_iam_role" "ecs_task_role" {
  name = "${local.prefix}-ecs-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

# Policy attachment for ECS Task Role
resource "aws_iam_role_policy_attachment" "ecs_task_role_policy_attachment" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_task_role_policy.arn
}

# An Task Role Policy for ECS
resource "aws_iam_policy" "ecs_task_role_policy" {
  name = "${local.prefix}-ecs-task-role-policy"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Action" : [
            "appconfig:StartConfigurationSession",
            "appconfig:GetLatestConfiguration"
          ],
          "Effect" : "Allow",
          "Resource" : "*"
        }
      ]
  })
}

# VPC
data "aws_vpc" "vpc" {
  id = var.vpc_id
}

# Security Group for ECS Task
resource "aws_security_group" "ecs_task_security_group" {
  name        = "${local.prefix}-ecs-task-security-group"
  description = "ECS Task Security Group"
  vpc_id      = data.aws_vpc.vpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
