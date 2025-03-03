package main

import (
	"encoding/json"
	"fmt"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/acm"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/apigatewayv2"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/iam"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/lb"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Load configuration
		config := config.New(ctx, "clash-bot-services")
		apiGatewayName := config.Require("api_gateway_name")
		apigatewayId := config.Require("api_gateway_id")
		loadBalancerArn := config.Require("load_balancer_arn")
		subnetIdOne := config.Require("subnet_id_one")
		subnetIdTwo := config.Require("subnet_id_two")
		path := config.Require("path")
		domainName := "clash-bot.ninja"

		fmt.Printf("apiGatewayName: %s\n", apiGatewayName)
		fmt.Printf("apigatewayId: %s\n", apigatewayId)

		// API Gateway
		api, err := apigatewayv2.LookupApi(ctx, &apigatewayv2.LookupApiArgs{
			ApiId: apigatewayId,
		})
		if err != nil {
			return fmt.Errorf("failed to lookup API Gateway: %w", err)
		}

		loadBalancer, err := lb.LookupLoadBalancer(ctx, &lb.LookupLoadBalancerArgs{
			Arn: pulumi.StringRef(loadBalancerArn),
		})
		if err != nil {
			return fmt.Errorf("failed to lookup Load Balancer: %w", err)
		}

		// Security Group for VPCLink
		vpcLinkSecurityGroup, err := ec2.NewSecurityGroup(ctx, "clash-bot-service-vpc-link-sg", &ec2.SecurityGroupArgs{
			VpcId:       pulumi.String(loadBalancer.VpcId),
			Description: pulumi.String("Gating traffic to the ECS service"),
		})
		if err != nil {
			return fmt.Errorf("failed to create Security Group: %w", err)
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "clash-bot-service-vpc-link-sg-ingress", &ec2.SecurityGroupRuleArgs{
			FromPort:        pulumi.Int(443),
			ToPort:          pulumi.Int(443),
			Protocol:        pulumi.String(ec2.ProtocolTypeTCP),
			Type:            pulumi.String("ingress"),
			SecurityGroupId: vpcLinkSecurityGroup.ID(),
			CidrBlocks: pulumi.StringArray{
				pulumi.String("0.0.0.0/0"),
			},
		})
		if err != nil {
			return fmt.Errorf("failed to create Security Group ingress rule: %w", err)
		}

		_, err = ec2.NewSecurityGroupRule(ctx, "clash-bot-service-vpc-link-sg-egress", &ec2.SecurityGroupRuleArgs{
			FromPort:        pulumi.Int(0),
			ToPort:          pulumi.Int(0),
			Protocol:        pulumi.String("-1"),
			Type:            pulumi.String("egress"),
			SecurityGroupId: vpcLinkSecurityGroup.ID(),
			CidrBlocks: pulumi.StringArray{
				pulumi.String("0.0.0.0/0"),
			},
		})
		if err != nil {
			return fmt.Errorf("failed to create Security Group egress rule: %w", err)
		}

		// VPC Link
		vpcLink, err := apigatewayv2.NewVpcLink(ctx, "clash-bot-service-vpc-link", &apigatewayv2.VpcLinkArgs{
			Name: pulumi.String("clash-bot-service-vpc-link"),

			SecurityGroupIds: pulumi.StringArray{
				vpcLinkSecurityGroup.ID(),
			},
			SubnetIds: pulumi.ToStringArray([]string{
				subnetIdOne,
				subnetIdTwo,
			}),
		})
		if err != nil {
			return fmt.Errorf("failed to create VPC Link: %w", err)
		}

		cert, err := acm.LookupCertificate(ctx, &acm.LookupCertificateArgs{
			Domain:   pulumi.StringRef(domainName),
			Statuses: []string{"ISSUED"},
		}, nil)
		if err != nil {
			return fmt.Errorf("failed to lookup certificate: %w", err)
		}

		loadBalancerListener, err := lb.LookupListener(ctx, &lb.LookupListenerArgs{
			LoadBalancerArn: pulumi.StringRef(loadBalancerArn),
			Port:            pulumi.IntRef(443),
		})

		if err != nil {
			return fmt.Errorf("failed to lookup Load Balancer Listener: %w", err)
		}

		// Create VPC Link IAM Policy
		tmpJSON0, err := json.Marshal(map[string]interface{}{
			"Version": "2012-10-17",
			"Statement": []map[string]interface{}{
				{
					"Action": []string{
						"ec2:CreateNetworkInterface",
						"ec2:DescribeNetworkInterfaces",
						"ec2:DeleteNetworkInterface",
						// Policy for Cloudwatch Logs
						"logs:CreateLogGroup",
						"logs:CreateLogStream",
						"logs:DescribeLogGroups",
						"logs:DescribeLogStreams",
						"logs:PutLogEvents",
						"logs:GetLogEvents",
						"logs:FilterLogEvents",
					},
					"Effect":   "Allow",
					"Resource": "*",
				},
			},
		})
		if err != nil {
			return err
		}
		json0 := string(tmpJSON0)
		policy, err := iam.NewPolicy(ctx, "policy", &iam.PolicyArgs{
			Name:        pulumi.String("vpc_link_policy"),
			Path:        pulumi.String("/"),
			Description: pulumi.String("IAM Policy for VPC Link for Clash Bot Service"),
			Policy:      pulumi.String(json0),
		})
		if err != nil {
			return err
		}

		// IAM Role for VPC Link
		role, err := iam.NewRole(ctx, "clash-bot-service-vpc-link-role", &iam.RoleArgs{
			Name:        pulumi.String("clash-bot-service-vpc-link-role"),
			Description: pulumi.String("IAM Role for VPC Link"),
			AssumeRolePolicy: pulumi.String(`{
				"Version": "2012-10-17",
				"Statement": [
					{
						"Effect": "Allow",
						"Principal": {
							"Service": "apigateway.amazonaws.com"
						},
						"Action": "sts:AssumeRole"
					}
				]
			}`),
		})
		if err != nil {
			return fmt.Errorf("failed to create IAM Role: %w", err)
		}

		iam.NewRolePolicyAttachment(ctx, "policyAttachment", &iam.RolePolicyAttachmentArgs{
			PolicyArn: policy.Arn,
			Role:      role.Name,
		})

		// API Gateway Integration
		integration, err := apigatewayv2.NewIntegration(ctx, "ecsIntegration443", &apigatewayv2.IntegrationArgs{
			ApiId:                pulumi.String(api.ApiId),
			Description:          pulumi.String("Integration with Clash Bot Service"),
			CredentialsArn:       role.Arn,
			IntegrationType:      pulumi.String("HTTP_PROXY"),
			IntegrationMethod:    pulumi.String("ANY"),
			IntegrationUri:       pulumi.String(loadBalancerListener.Arn),
			ConnectionType:       pulumi.String("VPC_LINK"),
			ConnectionId:         vpcLink.ID(),
			PayloadFormatVersion: pulumi.String("1.0"),
			TlsConfig: &apigatewayv2.IntegrationTlsConfigArgs{
				ServerNameToVerify: pulumi.String(cert.Domain),
			},
		})
		if err != nil {
			return fmt.Errorf("failed to create API Gateway Integration: %w", err)
		}

		// API Gateway Route
		_, err = apigatewayv2.NewRoute(ctx, "ecsRoute", &apigatewayv2.RouteArgs{
			ApiId:    pulumi.String(api.ApiId),
			RouteKey: pulumi.Sprintf("ANY %s/{proxy+}", path),
			Target:   pulumi.Sprintf("integrations/%s", integration.ID()),
		})
		if err != nil {
			return fmt.Errorf("failed to create API Gateway Route: %w", err)
		}

		// API Gateway Stage
		_, err = apigatewayv2.NewStage(ctx, "ecsStage", &apigatewayv2.StageArgs{
			ApiId:      pulumi.String(api.ApiId),
			Name:       pulumi.String("$default"),
			AutoDeploy: pulumi.Bool(true),
			DefaultRouteSettings: &apigatewayv2.StageDefaultRouteSettingsArgs{
				DataTraceEnabled:     pulumi.Bool(false),
				ThrottlingBurstLimit: pulumi.Int(100),
				ThrottlingRateLimit:  pulumi.Float64(50),
			},
		})
		if err != nil {
			return fmt.Errorf("failed to create API Gateway Stage: %w", err)
		}

		// Outputs
		ctx.Export("apiGatewayUrl", pulumi.String(api.ApiEndpoint))
		ctx.Export("vpcLink", vpcLink.ID())

		return nil
	})
}
