package com.united.stack;

import lombok.Getter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.VpcLink;
import software.amazon.awscdk.services.apigateway.VpcLinkProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancerProps;
import software.constructs.Construct;

import java.util.Collections;

@Getter
public class LoadBalancerStack extends Stack {

    private final VpcLink vpcLink;
    private final NetworkLoadBalancer networkLoadBalancer;
    private final ApplicationLoadBalancer applicationLoadBalancer;

    public LoadBalancerStack(final Construct scope, final String id, final StackProps props, CloudFormationStackProps cfProps) {
        super(scope, id, props);

        this.networkLoadBalancer = new NetworkLoadBalancer(this, "NetworkLoadBalancer", NetworkLoadBalancerProps.builder()
                .loadBalancerName("ECommerce-NLB")
                .internetFacing(false)
                .vpc(cfProps.vpc())
                .build());

        this.vpcLink = new VpcLink(this, "VpcLink", VpcLinkProps.builder()
                .targets(Collections.singletonList(this.networkLoadBalancer))
                .vpcLinkName("ECommerceVpcLink")
                .build());

        this.applicationLoadBalancer = new ApplicationLoadBalancer(this, "ApplicationLoadBalancer", ApplicationLoadBalancerProps.builder()
                .loadBalancerName("ECommerce-ALB")
                .internetFacing(false)
                .vpc(cfProps.vpc())
                .build());
    }
}

