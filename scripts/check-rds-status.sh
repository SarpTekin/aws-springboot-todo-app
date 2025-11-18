#!/bin/bash

# Script to check RDS instance status
# Usage: ./scripts/check-rds-status.sh

REGION=${AWS_DEFAULT_REGION:-$(aws configure get region)}

echo "=== RDS Instance Status ==="
echo "Region: $REGION"
echo ""

echo "User DB (user-db):"
aws rds describe-db-instances --db-instance-identifier user-db --region $REGION \
    --query 'DBInstances[0].[DBInstanceStatus,Endpoint.Address,Endpoint.Port]' \
    --output table

echo ""
echo "Task DB (task-db):"
aws rds describe-db-instances --db-instance-identifier task-db --region $REGION \
    --query 'DBInstances[0].[DBInstanceStatus,Endpoint.Address,Endpoint.Port]' \
    --output table

echo ""
echo "Status meanings:"
echo "  - creating: Instance is being created (5-10 minutes)"
echo "  - available: Instance is ready to use ✅"
echo "  - configuring-enhanced-monitoring: Almost ready"

