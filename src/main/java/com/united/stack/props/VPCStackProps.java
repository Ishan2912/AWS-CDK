package com.united.stack.props;

import lombok.Builder;
import software.amazon.awscdk.services.ec2.Vpc;

@Builder
public record VPCStackProps(
        Vpc vpc
){}
