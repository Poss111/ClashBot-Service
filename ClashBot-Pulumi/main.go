package main

import (
	"encoding/json"

	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/acm"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/ec2"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/ecs"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/iam"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/lb"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Variables
		vpcID := "vpc-04738ea3a5ae3ba52"
		subnetOneID := "subnet-092e50e03e16be078"
		subnetTwoID := "subnet-02d5547ced2a60b1d"
		domainName := "clash-bot.ninja"
		const image = "0.0.11-adding-path"

		vpcDetails, err := ec2.LookupVpc(ctx, &ec2.LookupVpcArgs{
			Id: &vpcID,
		})

		if err != nil {
			return err
		}

		subnetOne, err := ec2.LookupSubnet(ctx, &ec2.LookupSubnetArgs{
			VpcId: &vpcDetails.Id,
			Id:    &subnetOneID,
		})

		subnetTwo, err := ec2.LookupSubnet(ctx, &ec2.LookupSubnetArgs{
			VpcId: &vpcDetails.Id,
			Id:    &subnetTwoID,
		})

		// IAM Roles and Policies
		executionRole, err := iam.NewRole(ctx, "ecsTaskExecutionRole", &iam.RoleArgs{
			AssumeRolePolicy: pulumi.String(`{
                "Version": "2012-10-17",
                "Statement": [{
                    "Effect": "Allow",
                    "Principal": {"Service": "ecs-tasks.amazonaws.com"},
                    "Action": "sts:AssumeRole"
                }]
            }`),
		})
		if err != nil {
			return err
		}

		executionPolicy, err := iam.NewPolicy(ctx, "ecsTaskExecutionRolePolicy", &iam.PolicyArgs{
			Policy: pulumi.String(`{
                "Version": "2012-10-17",
                "Statement": [{
                    "Action": [
                        "ecr:GetAuthorizationToken",
                        "ecr:BatchCheckLayerAvailability",
                        "ecr:GetDownloadUrlForLayer",
                        "ecr:BatchGetImage",
                        "logs:CreateLogGroup",
                        "logs:CreateLogStream",
                        "logs:PutLogEvents",
                        "secretsmanager:GetSecretValue"
                    ],
                    "Effect": "Allow",
                    "Resource": "*"
                }]
            }`),
		})
		if err != nil {
			return err
		}

		_, err = iam.NewRolePolicyAttachment(ctx, "ecsTaskExecutionRolePolicyAttachment", &iam.RolePolicyAttachmentArgs{
			Role:      executionRole.Name,
			PolicyArn: executionPolicy.Arn,
		})
		if err != nil {
			return err
		}

		taskRole, err := iam.NewRole(ctx, "ecsTaskRole", &iam.RoleArgs{
			AssumeRolePolicy: pulumi.String(`{
                "Version": "2012-10-17",
                "Statement": [{
                    "Effect": "Allow",
                    "Principal": {"Service": "ecs-tasks.amazonaws.com"},
                    "Action": "sts:AssumeRole"
                }]
            }`),
		})
		if err != nil {
			return err
		}

		taskPolicy, err := iam.NewPolicy(ctx, "ecsTaskRolePolicy", &iam.PolicyArgs{
			Policy: pulumi.String(`{
                "Version": "2012-10-17",
                "Statement": [{
                    "Action": [
                        "appconfig:StartConfigurationSession",
                        "appconfig:GetLatestConfiguration"
                    ],
                    "Effect": "Allow",
                    "Resource": "*"
                }]
            }`),
		})
		if err != nil {
			return err
		}

		_, err = iam.NewRolePolicyAttachment(ctx, "ecsTaskRolePolicyAttachment", &iam.RolePolicyAttachmentArgs{
			Role:      taskRole.Name,
			PolicyArn: taskPolicy.Arn,
		})
		if err != nil {
			return err
		}

		// Security Groups
		ecsSecurityGroup, err := ec2.NewSecurityGroup(ctx, "ecsTaskSecurityGroup", &ec2.SecurityGroupArgs{
			Description: pulumi.String("ECS Task Security Group"),
			VpcId:       pulumi.String(vpcID),
		})
		if err != nil {
			return err
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "ecsTaskSecurityGroupIngress", &ec2.SecurityGroupRuleArgs{
			Type:            pulumi.String("ingress"),
			SecurityGroupId: ecsSecurityGroup.ID(),
			FromPort:        pulumi.Int(0),
			ToPort:          pulumi.Int(65535),
			Protocol:        pulumi.String("tcp"),
			CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		})
		if err != nil {
			return err
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "ecsTaskSecurityGroupEgress", &ec2.SecurityGroupRuleArgs{
			Type:            pulumi.String("egress"),
			SecurityGroupId: ecsSecurityGroup.ID(),
			FromPort:        pulumi.Int(0),
			ToPort:          pulumi.Int(65535),
			Protocol:        pulumi.String("-1"),
			CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		})
		if err != nil {
			return err
		}

		lbSecurityGroup, err := ec2.NewSecurityGroup(ctx, "clashBotLbSecurityGroup", &ec2.SecurityGroupArgs{
			Description: pulumi.String("Clash Bot Load Balancer Security Group"),
			VpcId:       pulumi.String(vpcID),
		})
		if err != nil {
			return err
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "lbSecurityGroupIngress", &ec2.SecurityGroupRuleArgs{
			Type:            pulumi.String("ingress"),
			SecurityGroupId: lbSecurityGroup.ID(),
			FromPort:        pulumi.Int(443),
			ToPort:          pulumi.Int(443),
			Protocol:        pulumi.String("tcp"),
			CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		})
		if err != nil {
			return err
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "lbSecurityGroupEgress", &ec2.SecurityGroupRuleArgs{
			Type:            pulumi.String("egress"),
			SecurityGroupId: lbSecurityGroup.ID(),
			FromPort:        pulumi.Int(0),
			ToPort:          pulumi.Int(65535),
			Protocol:        pulumi.String("-1"),
			CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		})
		if err != nil {
			return err
		}

		// Load Balancer
		alb, err := lb.NewLoadBalancer(ctx, "clashBotLb", &lb.LoadBalancerArgs{
			Name:             pulumi.String("clash-bot-service-lb"),
			Internal:         pulumi.Bool(false),
			LoadBalancerType: pulumi.String("application"),
			SecurityGroups:   pulumi.StringArray{lbSecurityGroup.ID()},
			Subnets:          pulumi.StringArray{pulumi.String(subnetOne.Id), pulumi.String(subnetTwo.Id)},
			AccessLogs: &lb.LoadBalancerAccessLogsArgs{
				Bucket:  pulumi.String("logs.clash-bot-service.us-east-1"),
				Prefix:  pulumi.String("clash-bot-service-lb"),
				Enabled: pulumi.Bool(true),
			},
		})
		if err != nil {
			return err
		}

		targetGroup, err := lb.NewTargetGroup(ctx, "ecsTg", &lb.TargetGroupArgs{
			Name:     pulumi.String("clash-bot-ecs-target-group"),
			Port:     pulumi.Int(443),
			Protocol: pulumi.String("HTTPS"),
			VpcId:    pulumi.String(vpcID),
			HealthCheck: &lb.TargetGroupHealthCheckArgs{
				Path:               pulumi.String("/clash-bot/actuator/health/readiness"),
				Protocol:           pulumi.String("HTTPS"),
				Port:               pulumi.String("8080"),
				Interval:           pulumi.Int(60),
				Timeout:            pulumi.Int(5),
				HealthyThreshold:   pulumi.Int(2),
				UnhealthyThreshold: pulumi.Int(5),
			},
			TargetType: pulumi.String("ip"),
		})
		if err != nil {
			return err
		}

		cert, err := acm.LookupCertificate(ctx, &acm.LookupCertificateArgs{
			Domain:   domainName,
			Statuses: []string{"ISSUED"},
		}, nil)
		if err != nil {
			return err
		}

		_, err = lb.NewListener(ctx, "clashBotLbListener", &lb.ListenerArgs{
			LoadBalancerArn: alb.Arn,
			Port:            pulumi.Int(443),
			Protocol:        pulumi.String("HTTPS"),
			SslPolicy:       pulumi.String("ELBSecurityPolicy-2016-08"),
			CertificateArn:  pulumi.String(cert.Arn),
			DefaultActions: lb.ListenerDefaultActionArray{
				&lb.ListenerDefaultActionArgs{
					Type:           pulumi.String("forward"),
					TargetGroupArn: targetGroup.Arn,
				},
			},
		})
		if err != nil {
			return err
		}

		// iam, err := iam.LookupRole(ctx, &iam.LookupRoleArgs{
		// 	Name: "AWSServiceRoleForECS",
		// })

		// if err != nil {
		// 	return err
		// }

		tmpJSON0, err := json.Marshal([]interface{}{
			map[string]interface{}{
				"name":      "clash-bot",
				"image":     "816923827429.dkr.ecr.us-east-1.amazonaws.com/busybox:latest",
				"cpu":       256,
				"memory":    512,
				"essential": true,
				"portMappings": []map[string]interface{}{
					map[string]interface{}{
						"containerPort": 8080,
						"hostPort":      8080,
					},
				},
				"command": []string{
					"sh",
					"-c",
					"while true; do echo 'Hello, World!'; sleep 1; done",
				},
			},
		})

		if err != nil {
			return err
		}

		taskDef, err := ecs.NewTaskDefinition(ctx, "busy-box-task", &ecs.TaskDefinitionArgs{
			Family:      pulumi.String("busy-box-family"),
			Cpu:         pulumi.String("256"),
			Memory:      pulumi.String("512"),
			NetworkMode: pulumi.String("awsvpc"),

			RequiresCompatibilities: pulumi.StringArray{
				pulumi.String("FARGATE"),
			},
			ContainerDefinitions: pulumi.String(tmpJSON0),
			ExecutionRoleArn:     executionRole.Arn,
			TaskRoleArn:          taskRole.Arn,
		})

		if err != nil {
			return err
		}

		ecs.NewService(ctx, "clashBotService", &ecs.ServiceArgs{
			Cluster:      pulumi.String("main-cluster"),
			DesiredCount: pulumi.Int(1),
			LaunchType:   pulumi.String("FARGATE"),
			NetworkConfiguration: &ecs.ServiceNetworkConfigurationArgs{
				AssignPublicIp: pulumi.Bool(true),
				SecurityGroups: pulumi.StringArray{ecsSecurityGroup.ID()},
				Subnets: pulumi.StringArray{
					pulumi.String(subnetOne.Id),
					pulumi.String(subnetTwo.Id),
				},
			},
			LoadBalancers: ecs.ServiceLoadBalancerArray{
				&ecs.ServiceLoadBalancerArgs{
					ContainerName:  pulumi.String("clash-bot"),
					ContainerPort:  pulumi.Int(8080),
					TargetGroupArn: targetGroup.Arn,
				},
			},
			TaskDefinition: taskDef.Arn,
		}, pulumi.IgnoreChanges([]string{
			"taskDefinition",
			"desiredCount",
		}))

		if err != nil {
			return err
		}

		// Outputs
		ctx.Export("ecsTaskExecutionRoleArn", executionRole.Arn)
		ctx.Export("ecsTaskRoleArn", taskRole.Arn)
		ctx.Export("ecsServiceSecurityGroupId", ecsSecurityGroup.ID())
		ctx.Export("loadBalancerDns", alb.DnsName)
		ctx.Export("loadBalancerArn", alb.Arn)
		ctx.Export("loadBalancerType", alb.LoadBalancerType)
		ctx.Export("loadBalancerName", alb.Name)
		ctx.Export("targetGroupArn", targetGroup.Arn)

		return nil
	})
}
