package com.united.stack;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.AddApplicationTargetsProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.AddNetworkTargetsProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseNetworkListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkListenerProps;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductsServiceStack extends Stack {

    public ProductsServiceStack(final Construct scope, final String id,
                                final StackProps props,
                                ProductsServiceStackProps productsServiceStackProps) {
        super(scope, id, props);

        FargateTaskDefinition fargateTaskDefinition = new FargateTaskDefinition(this, "TaskDefinition",
                FargateTaskDefinitionProps.builder()
                        .family("products-service")
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .build());

        AwsLogDriver logDriver = new AwsLogDriver(AwsLogDriverProps.builder()
                .logGroup(new LogGroup(this, "LogGroup",
                        LogGroupProps.builder()
                                .logGroupName("ProductsService")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_WEEK)
                                .build()))
                .streamPrefix("ProductsService")
                .build());

        Map<String, String> environment = new HashMap<>();
        environment.put("SERVER_PORT", "8080");

        fargateTaskDefinition.addContainer("ProductsServiceContainer",
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromEcrRepository(productsServiceStackProps.repository(),
                                "latest"))
                        .containerName("products-service")
                        .logging(logDriver)
                        .portMappings(Collections.singletonList(PortMapping.builder()
                                .containerPort(8080)
                                .hostPort(8080)
                                .protocol(Protocol.TCP)
                                .build()))
                        .environment(environment)
                        .build());

        ApplicationListener applicationListener = productsServiceStackProps.applicationLoadBalancer()
                .addListener("ProductsServiceALBListener", ApplicationListenerProps.builder()
                        .port(8080)
                        .protocol(ApplicationProtocol.HTTP)
                        .loadBalancer(productsServiceStackProps.applicationLoadBalancer())
                        .build());

        FargateService fargateService = new FargateService(this, "ProductsService",
                FargateServiceProps.builder()
                        .serviceName("ProductsService")
                        .cluster(productsServiceStackProps.cluster())
                        .taskDefinition(fargateTaskDefinition)
                        .desiredCount(2)
//                        .assignPublicIp(true)
                        .build());
        productsServiceStackProps.repository().grantPull(Objects.requireNonNull(fargateTaskDefinition.getExecutionRole()));
        fargateService.getConnections().getSecurityGroups().get(0).addIngressRule(Peer.anyIpv4(), Port.tcp(8080));

        applicationListener.addTargets("ProductsServiceALBTarget",
                AddApplicationTargetsProps.builder()
                        .targetGroupName("ProductsServiceALB")
                        .port(8080)
                        .protocol(ApplicationProtocol.HTTP)
                        .targets(Collections.singletonList(fargateService))
                        .deregistrationDelay(Duration.seconds(30))
                        .healthCheck(HealthCheck.builder()
                                .enabled(true)
                                .interval(Duration.seconds(30))
                                .timeout(Duration.seconds(10))
                                .path("/actuator/health")
                                .port("8080")
                                .build())
                        .build());

        NetworkListener networkListener = productsServiceStackProps.networkLoadBalancer()
                .addListener("ProductsServiceNLBListener", BaseNetworkListenerProps.builder()
                        .port(8080)
                        .protocol(
                                software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.TCP
                        )
                        .build());
        networkListener.addTargets("ProductsServiceNLBTarget",
                AddNetworkTargetsProps.builder()
                        .port(8080)
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.TCP)
                        .targetGroupName("ProductsServiceNLB")
                        .targets(Collections.singletonList(fargateService.loadBalancerTarget(
                                LoadBalancerTargetOptions.builder()
                                        .containerName("ProductsService")
                                        .containerPort(8080)
                                        .protocol(Protocol.TCP)
                                        .build())))
                        .build());
    }

}

