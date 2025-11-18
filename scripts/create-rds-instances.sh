#!/bin/bash

# Script to create RDS PostgreSQL instances for user-service and task-service
# Usage: ./scripts/create-rds-instances.sh

set -e

REGION=${AWS_DEFAULT_REGION:-$(aws configure get region)}
DB_INSTANCE_CLASS="db.t3.micro"  # Free tier eligible
ENGINE_VERSION="16.8"  # Use available version in eu-central-1
STORAGE_SIZE=20
MASTER_USERNAME="dbadmin"  # 'admin' is reserved in PostgreSQL
BACKUP_RETENTION=1  # Free tier allows max 1 day

echo "=== Creating RDS PostgreSQL Instances ==="
echo "Region: $REGION"
echo ""

# Generate secure passwords
USER_DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
TASK_DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)

echo "Generated passwords (save these!):"
echo "User DB Password: $USER_DB_PASSWORD"
echo "Task DB Password: $TASK_DB_PASSWORD"
echo ""

# Create user-db
echo "Creating user-db instance..."
aws rds create-db-instance \
    --db-instance-identifier user-db \
    --db-instance-class $DB_INSTANCE_CLASS \
    --engine postgres \
    --engine-version $ENGINE_VERSION \
    --master-username $MASTER_USERNAME \
    --master-user-password "$USER_DB_PASSWORD" \
    --allocated-storage $STORAGE_SIZE \
    --db-name userdb \
    --region $REGION \
    --publicly-accessible \
    --storage-type gp2 \
    --backup-retention-period $BACKUP_RETENTION \
    --no-multi-az \
    --no-deletion-protection \
    --tags Key=Name,Value=user-db Key=Service,Value=user-service || echo "user-db may already exist"

# Create task-db
echo "Creating task-db instance..."
aws rds create-db-instance \
    --db-instance-identifier task-db \
    --db-instance-class $DB_INSTANCE_CLASS \
    --engine postgres \
    --engine-version $ENGINE_VERSION \
    --master-username $MASTER_USERNAME \
    --master-user-password "$TASK_DB_PASSWORD" \
    --allocated-storage $STORAGE_SIZE \
    --db-name taskdb \
    --region $REGION \
    --publicly-accessible \
    --storage-type gp2 \
    --backup-retention-period $BACKUP_RETENTION \
    --no-multi-az \
    --no-deletion-protection \
    --tags Key=Name,Value=task-db Key=Service,Value=task-service || echo "task-db may already exist"

echo ""
echo "=== RDS Instances Creation Initiated ==="
echo ""
echo "⚠️  IMPORTANT: Save these passwords!"
echo "User DB Password: $USER_DB_PASSWORD"
echo "Task DB Password: $TASK_DB_PASSWORD"
echo ""
echo "Instances will take 5-10 minutes to be available."
echo "Check status with: aws rds describe-db-instances --region $REGION"
echo ""
echo "Once available, get endpoints with:"
echo "aws rds describe-db-instances --db-instance-identifier user-db --query 'DBInstances[0].Endpoint.Address' --output text"
echo "aws rds describe-db-instances --db-instance-identifier task-db --query 'DBInstances[0].Endpoint.Address' --output text"

