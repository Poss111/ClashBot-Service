package main

import (
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/apigatewayv2"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/lb"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Load configuration
		config := config.New(ctx, "clash-bot-services")
		loadBalancerArn := config.Require("load_balancer_arn")
		path := config.Require("path")

		// API Gateway
		api, err := apigatewayv2.NewApi(ctx, "ecsApi", &apigatewayv2.ApiArgs{
			Name:         pulumi.String("clash-bot-services-apis"),
			ProtocolType: pulumi.String("HTTP"),
			Description:  pulumi.String("API Gateway for Clash Bot Services"),
			CorsConfiguration: &apigatewayv2.ApiCorsConfigurationArgs{
				AllowMethods: pulumi.StringArray{
					pulumi.String("GET"),
					pulumi.String("POST"),
					pulumi.String("PUT"),
					pulumi.String("DELETE"),
					pulumi.String("OPTIONS"),
				},
				AllowOrigins: pulumi.StringArray{
					pulumi.String("*"),
				},
			},
		})
		if err != nil {
			return err
		}

		loadBalancer, err := lb.LookupLoadBalancer(ctx, &lb.LookupLoadBalancerArgs{
			Arn: pulumi.StringRef(loadBalancerArn),
		}, nil)

		if err != nil {
			return err
		}

		// API Gateway Integration
		integration, err := apigatewayv2.NewIntegration(ctx, "ecsIntegration443", &apigatewayv2.IntegrationArgs{
			ApiId:                api.ID(),
			IntegrationType:      pulumi.String("HTTP_PROXY"),
			IntegrationMethod:    pulumi.String("ANY"),
			IntegrationUri:       pulumi.Sprintf("http://%s%s/{proxy}", loadBalancer.DnsName, path),
			PayloadFormatVersion: pulumi.String("1.0"),
		})
		if err != nil {
			return err
		}

		// API Gateway Route
		_, err = apigatewayv2.NewRoute(ctx, "ecsRoute", &apigatewayv2.RouteArgs{
			ApiId:    api.ID(),
			RouteKey: pulumi.Sprintf("ANY %s/{proxy+}", path),
			Target:   pulumi.Sprintf("integrations/%s", integration.ID()),
		})
		if err != nil {
			return err
		}

		// API Gateway Stage
		_, err = apigatewayv2.NewStage(ctx, "ecsStage", &apigatewayv2.StageArgs{
			ApiId:      api.ID(),
			Name:       pulumi.String("$default"),
			AutoDeploy: pulumi.Bool(true),
			DefaultRouteSettings: &apigatewayv2.StageDefaultRouteSettingsArgs{
				DataTraceEnabled:     pulumi.Bool(false),
				ThrottlingBurstLimit: pulumi.Int(100),
				ThrottlingRateLimit:  pulumi.Float64(50),
			},
		})
		if err != nil {
			return err
		}

		// Outputs
		ctx.Export("apiGatewayUrl", api.ApiEndpoint)

		return nil
	})
}
