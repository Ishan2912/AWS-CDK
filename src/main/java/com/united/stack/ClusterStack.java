package com.united.stack;

import lombok.Getter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterProps;
import software.constructs.Construct;

@Getter
public class ClusterStack extends Stack {

    private final Cluster cluster;

    public ClusterStack(final Construct scope, final String id, final StackProps props, CloudFormationStackProps clusterProps) {
        super(scope, id, props);

        this.cluster = new Cluster(this,id + "Cluster", ClusterProps.builder()
                .clusterName("ECommerce")
                .vpc(clusterProps.vpc())
                .containerInsights(true)
                .build());
    }

}


