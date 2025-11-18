#!/bin/bash

# Script to set up AWS Secrets Manager for RDS credentials and JWT secret
# Usage: ./scripts/setup-secrets-manager.sh [user-db-password] [task-db-password]

set -e

REGION=${AWS_DEFAULT_REGION:-$(aws configure get region)}

if [ $# -lt 2 ]; then
    echo "Usage: $0 <user-db-password> <task-db-password>"
    echo "Example: $0 'MySecurePass123!' 'AnotherSecurePass456!'"
    exit 1
fi

USER_DB_PASSWORD=$1
TASK_DB_PASSWORD=$2
JWT_SECRET="WGhzVGZqNXdKSmJvcG1lT1BoU3pBRFNlcnZKeWNYR1c="

echo "=== Setting up AWS Secrets Manager ==="
echo "Region: $REGION"
echo ""

# Get RDS endpoints
echo "Getting RDS endpoints..."
USER_DB_ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier user-db --region $REGION --query 'DBInstances[0].Endpoint.Address' --output text 2>/dev/null || echo "")
TASK_DB_ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier task-db --region $REGION --query 'DBInstances[0].Endpoint.Address' --output text 2>/dev/null || echo "")

if [ -z "$USER_DB_ENDPOINT" ] || [ "$USER_DB_ENDPOINT" == "None" ]; then
    echo "⚠️  Warning: user-db endpoint not found. RDS instance may still be creating."
    USER_DB_ENDPOINT="user-db.xxxxx.rds.amazonaws.com"  # Placeholder
fi

if [ -z "$TASK_DB_ENDPOINT" ] || [ "$TASK_DB_ENDPOINT" == "None" ]; then
    echo "⚠️  Warning: task-db endpoint not found. RDS instance may still be creating."
    TASK_DB_ENDPOINT="task-db.xxxxx.rds.amazonaws.com"  # Placeholder
fi

# Create RDS credentials secret
echo "Creating RDS credentials secret..."
aws secretsmanager create-secret \
    --name rds-credentials \
    --description "RDS database credentials for user-db and task-db" \
    --secret-string "{\"username\":\"dbadmin\",\"userDbPassword\":\"$USER_DB_PASSWORD\",\"taskDbPassword\":\"$TASK_DB_PASSWORD\"}" \
    --region $REGION 2>/dev/null || \
aws secretsmanager update-secret \
    --secret-id rds-credentials \
    --secret-string "{\"username\":\"dbadmin\",\"userDbPassword\":\"$USER_DB_PASSWORD\",\"taskDbPassword\":\"$TASK_DB_PASSWORD\"}" \
    --region $REGION

# Create JWT secret
echo "Creating JWT secret..."
aws secretsmanager create-secret \
    --name jwt-secret \
    --description "JWT secret key for authentication" \
    --secret-string "{\"secret\":\"$JWT_SECRET\"}" \
    --region $REGION 2>/dev/null || \
aws secretsmanager update-secret \
    --secret-id jwt-secret \
    --secret-string "{\"secret\":\"$JWT_SECRET\"}" \
    --region $REGION

# Create RDS endpoint secrets
echo "Creating RDS endpoint secrets..."
aws secretsmanager create-secret \
    --name rds-user-db-endpoint \
    --description "RDS endpoint for user-db" \
    --secret-string "{\"endpoint\":\"$USER_DB_ENDPOINT\"}" \
    --region $REGION 2>/dev/null || \
aws secretsmanager update-secret \
    --secret-id rds-user-db-endpoint \
    --secret-string "{\"endpoint\":\"$USER_DB_ENDPOINT\"}" \
    --region $REGION

aws secretsmanager create-secret \
    --name rds-task-db-endpoint \
    --description "RDS endpoint for task-db" \
    --secret-string "{\"endpoint\":\"$TASK_DB_ENDPOINT\"}" \
    --region $REGION 2>/dev/null || \
aws secretsmanager update-secret \
    --secret-id rds-task-db-endpoint \
    --secret-string "{\"endpoint\":\"$TASK_DB_ENDPOINT\"}" \
    --region $REGION

echo ""
echo "=== Secrets Manager Setup Complete ==="
echo ""
echo "Created secrets:"
echo "  - rds-credentials"
echo "  - jwt-secret"
echo "  - rds-user-db-endpoint"
echo "  - rds-task-db-endpoint"
echo ""
echo "Note: Update endpoint secrets once RDS instances are fully available."

