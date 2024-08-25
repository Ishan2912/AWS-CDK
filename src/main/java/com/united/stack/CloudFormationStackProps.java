package com.united.stack;

import lombok.Builder;
import software.amazon.awscdk.services.ec2.Vpc;

@Builder
public record CloudFormationStackProps(
        Vpc vpc
){}
