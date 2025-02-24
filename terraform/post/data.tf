data "aws_vpc" "vpc" {
  id = var.vpc_id
}

# Subnets
data "aws_subnet" "subnet" {
  vpc_id = data.aws_vpc.vpc.id
  id     = var.subnet_one_id
}

# Subnets
data "aws_subnet" "subnet-two" {
  vpc_id = data.aws_vpc.vpc.id
  id     = var.subnet_two_id
}
