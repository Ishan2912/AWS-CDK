package com.united;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;
import java.util.Map;


public class AwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment environment = Environment.builder()
                .account("410336132773")
                .region("ap-south-1")
                .build();

        Map<String, String> infraTags = new HashMap<>();
        infraTags.put("team", "united");
        infraTags.put("cost", "ECommerceInfra");

        ECRStack ecrStack = new ECRStack(app, "ECR",
                StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build());

        VPCStack vpcStack = new VPCStack(app, "VPC",
                StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build());

        ClusterStack clusterStack = new ClusterStack(app, "Cluster",
                StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build(), new CloudFormationStackProps(vpcStack.getVpc()));
        clusterStack.addDependency(vpcStack);

        LoadBalancerStack  loadBalancerStack = new LoadBalancerStack(app, "LoadBalancer",
                StackProps.builder()
                        .env(environment)
                        .tags(infraTags)
                        .build(),
                        new CloudFormationStackProps(vpcStack.getVpc()));
        loadBalancerStack.addDependency(vpcStack);

        app.synth();
    }
}

