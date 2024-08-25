package com.united;

import com.united.stack.APIGatewayStack;
import com.united.stack.props.APIGatewayStackProps;
import com.united.stack.props.VPCStackProps;
import com.united.stack.ClusterStack;
import com.united.stack.ECRStack;
import com.united.stack.LoadBalancerStack;
import com.united.stack.ProductsServiceStack;
import com.united.stack.props.CommonStackProps;
import com.united.stack.VPCStack;
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
                .build(), new VPCStackProps(vpcStack.getVpc()));
        clusterStack.addDependency(vpcStack);

        LoadBalancerStack loadBalancerStack = new LoadBalancerStack(app, "LoadBalancer",
                StackProps.builder()
                        .env(environment)
                        .tags(infraTags)
                        .build(),
                        new VPCStackProps(vpcStack.getVpc()));
        loadBalancerStack.addDependency(vpcStack);


        Map<String, String> productsServiceTags = new HashMap<>();
        infraTags.put("team", "united");
        infraTags.put("cost", "ProductsService");
        ProductsServiceStack productsServiceStack = new ProductsServiceStack(app, "ProductsService", StackProps.builder()
                .env(environment)
                .tags(productsServiceTags)
                .build(),
                new CommonStackProps(
                        vpcStack.getVpc(),
                        clusterStack.getCluster(),
                        loadBalancerStack.getNetworkLoadBalancer(),
                        loadBalancerStack.getApplicationLoadBalancer(),
                        ecrStack.getProductsServiceRepository()));
        productsServiceStack.addDependency(vpcStack);
        productsServiceStack.addDependency(clusterStack);
        productsServiceStack.addDependency(loadBalancerStack);
        productsServiceStack.addDependency(ecrStack);

        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "API",
                StackProps.builder()
                        .env(environment)
                        .tags(infraTags)
                        .build(),
                        new APIGatewayStackProps(
                                loadBalancerStack.getVpcLink(),
                                loadBalancerStack.getNetworkLoadBalancer()));
        apiGatewayStack.addDependency(loadBalancerStack);

        app.synth();
    }
}

