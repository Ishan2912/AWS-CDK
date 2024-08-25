package com.united.stack.props;

import software.amazon.awscdk.services.apigateway.VpcLink;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;

public record APIGatewayStackProps(
        VpcLink vpcLink,
        NetworkLoadBalancer networkLoadBalancer) {
}
