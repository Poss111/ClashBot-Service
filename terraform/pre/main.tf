# Kinesis
# resource "aws_kinesis_stream" "kinesis_stream" {
#   name             = "kinesis-stream"
#   shard_count      = 1
#   retention_period = 24
#   stream_mode_details {
#     stream_mode = "ON_DEMAND"
#   }
# }

# An Task Execution Role for ECS
resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "ecs-task-execution-role"
  assume_role_policy = <<EOF
    {
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Effect": "Allow",
            "Resource": "*"
        },
        {
            "Action": "secretsmanager:GetSecretValue",
            "Effect": "Allow",
            "Resource": "arn:aws:secretsmanager:*:*:secret:prod/MrBot/*"
        },
        {
            "Action": "secretsmanager:GetSecretValue",
            "Effect": "Allow",
            "Resource": "arn:aws:secretsmanager:*:*:secret:RIOT_TOKEN*"
        }
    ]
    }
    EOF
}

# An Task Role for ECS
resource "aws_iam_role" "ecs_task_role" {
  name               = "ecs-task-role"
  assume_role_policy = <<EOF
    {
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "appconfig:StartConfigurationSession",
                "appconfig:GetLatestConfiguration"
            ],
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
    }
    EOF
}

# VPC
data "aws_vpc" "vpc" {
  id = var.vpc_id
}

# Security Group for ECS Task
resource "aws_security_group" "ecs_task_security_group" {
  name        = "ecs-task-security-group"
  description = "ECS Task Security Group"
  vpc_id      = data.aws_vpc.vpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
