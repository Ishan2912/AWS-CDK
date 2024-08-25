package com.united.stack;

import com.united.stack.props.APIGatewayStackProps;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.ConnectionType;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.IntegrationOptions;
import software.amazon.awscdk.services.apigateway.IntegrationProps;
import software.amazon.awscdk.services.apigateway.IntegrationType;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.constructs.Construct;

public class APIGatewayStack extends Stack {

    public APIGatewayStack(final Construct scope, final String id, final StackProps props,
                           APIGatewayStackProps apiGatewayStackProps) {
        super(scope, id, props);

        RestApi restApi = new RestApi(this, "RestApi",
                RestApiProps.builder()
                        .restApiName("ECommerceAPI")
                        .build());

        this.createProductsResource(restApi, apiGatewayStackProps);
    }

    private void createProductsResource(RestApi restApi, APIGatewayStackProps apiGatewayStackProps) {
        Resource productsResource = restApi.getRoot().addResource("products");
        productsResource.addMethod("GET", new Integration(
                IntegrationProps.builder()
                        .type(IntegrationType.HTTP_PROXY)
                        .integrationHttpMethod("GET")
                        .uri("http://" + apiGatewayStackProps.networkLoadBalancer().getLoadBalancerDnsName() +
                                ":8080/api/products")
                        .options(IntegrationOptions.builder()
                                .vpcLink(apiGatewayStackProps.vpcLink())
                                .connectionType(ConnectionType.VPC_LINK)
                                .build())
                        .build()
        ));

    }
}
