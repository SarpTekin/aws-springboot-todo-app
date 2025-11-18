# AWS Secrets Manager - Setup Summary

**Date:** November 18, 2025  
**Region:** eu-central-1  
**Status:** ✅ Complete

---

## Created Secrets

### 1. **rds-credentials**
- **Description:** RDS database credentials for user-db and task-db
- **Contains:**
  - `username`: dbadmin
  - `userDbPassword`: (stored securely)
  - `taskDbPassword`: (stored securely)
- **ARN:** `arn:aws:secretsmanager:eu-central-1:316489803246:secret:rds-credentials-v5JGe2`

### 2. **jwt-secret**
- **Description:** JWT secret key for authentication
- **Contains:**
  - `secret`: WGhzVGZqNXdKSmJvcG1lT1BoU3pBRFNlcnZKeWNYR1c=
- **ARN:** `arn:aws:secretsmanager:eu-central-1:316489803246:secret:jwt-secret-UzDbVI`

### 3. **rds-user-db-endpoint**
- **Description:** RDS endpoint for user-db
- **Contains:**
  - `endpoint`: user-db.cr66k82eqsca.eu-central-1.rds.amazonaws.com
- **ARN:** `arn:aws:secretsmanager:eu-central-1:316489803246:secret:rds-user-db-endpoint-PMKVYi`

### 4. **rds-task-db-endpoint**
- **Description:** RDS endpoint for task-db
- **Contains:**
  - `endpoint`: task-db.cr66k82eqsca.eu-central-1.rds.amazonaws.com
- **ARN:** `arn:aws:secretsmanager:eu-central-1:316489803246:secret:rds-task-db-endpoint-8L3I0t`

---

## How to Access Secrets

### Using AWS CLI:
```bash
# Get RDS credentials
aws secretsmanager get-secret-value --secret-id rds-credentials --region eu-central-1

# Get JWT secret
aws secretsmanager get-secret-value --secret-id jwt-secret --region eu-central-1

# Get endpoints
aws secretsmanager get-secret-value --secret-id rds-user-db-endpoint --region eu-central-1
aws secretsmanager get-secret-value --secret-id rds-task-db-endpoint --region eu-central-1
```

### In Elastic Beanstalk:
Secrets can be referenced in environment variables, but direct Secrets Manager integration requires IAM roles with appropriate permissions.

---

## Security Notes

- ✅ All secrets are encrypted at rest
- ✅ Secrets Manager provides automatic rotation capabilities (optional)
- ✅ Access is controlled via IAM policies
- ⚠️  Ensure Elastic Beanstalk instance roles have `secretsmanager:GetSecretValue` permission

---

## Next Steps

1. ✅ RDS instances created
2. ✅ Secrets Manager configured
3. ⏭️  Deploy to Elastic Beanstalk
4. ⏭️  Configure environment variables
5. ⏭️  Test deployment

