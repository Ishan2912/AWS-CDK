package com.united;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;
import java.util.Map;


public class EcsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment environment = Environment.builder()
                .account("410336132773")
                .region("ap-south-1")
                .build();

        Map<String, String> infraTags = new HashMap<>();
        infraTags.put("team", "united");
        infraTags.put("cost", "ECommerceInfra");

        EcrStack ecrStack = new EcrStack(app, "Ecr", StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build());

        app.synth();
    }
}

