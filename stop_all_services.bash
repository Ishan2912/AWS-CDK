#!/bin/bash

# Stop all running EC2 instances
instances=$(aws ec2 describe-instances --query "Reservations[].Instances[?State.Name=='running'].InstanceId" --output text)
if [ -n "$instances" ]; then
    aws ec2 stop-instances --instance-ids $instances
    echo "Stopping EC2 instances: $instances"
else
    echo "No running EC2 instances found."
fi

# Stop all RDS instances
db_instances=$(aws rds describe-db-instances --query "DBInstances[?DBInstanceStatus=='available'].DBInstanceIdentifier" --output text)
if [ -n "$db_instances" ]; then
    for db_instance in $db_instances; do
        aws rds stop-db-instance --db-instance-identifier $db_instance
        echo "Stopping RDS instance: $db_instance"
    done
else
    echo "No available RDS instances found."
fi

# Stop all ECS services and tasks
clusters=$(aws ecs list-clusters --query "clusterArns[]" --output text)
for cluster in $clusters; do
    services=$(aws ecs list-services --cluster $cluster --query "serviceArns[]" --output text)
    if [ -n "$services" ]; then
        for service in $services; do
            aws ecs update-service --cluster $cluster --service $service --desired-count 0
            echo "Scaling down ECS service: $service"
        done
    fi
done	

# List all Lambda functions
functions=$(aws lambda list-functions --query "Functions[].FunctionName" --output text)
for function in $functions; do
    # Remove triggers or disable the function if needed
    # For example, you can delete event source mappings or disable scheduled events
    echo "Lambda function: $function"
done

# Terminate all Elastic Beanstalk environments
environments=$(aws elasticbeanstalk describe-environments --query "Environments[?Status=='Ready'].EnvironmentName" --output text)
if [ -n "$environments" ]; then
    for environment in $environments; do
        aws elasticbeanstalk terminate-environment --environment-name $environment
        echo "Terminating Elastic Beanstalk environment: $environment"
    done
else
    echo "No Elastic Beanstalk environments found."
fi
