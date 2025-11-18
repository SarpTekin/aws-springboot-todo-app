# AWS Deployment Roadmap - No Docker/Lambda

**Goal:** Deploy Spring Boot microservices to AWS using Elastic Beanstalk (no Docker required).

---

## 📋 Prerequisites Checklist

Before starting, ensure you have:
- [ ] AWS Account created (use free tier where possible)
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Terraform installed (recommended) OR AWS Console access
- [ ] AWS credentials configured with appropriate permissions
- [ ] Spring Boot applications tested locally
- [ ] JAR files built and ready (`./mvnw clean package`)

**Required AWS Permissions:**
- RDS (create, modify, delete instances)
- Elastic Beanstalk (create environments, deploy applications)
- Secrets Manager (create, read secrets)
- VPC, Security Groups, IAM (create resources)
- CloudWatch (create log groups)
- Application Load Balancer (created automatically by Elastic Beanstalk)

---

## 🗓️ Timeline Overview

**Total Estimated Time: 2 weeks (working part-time)**

- **Week 1:** Infrastructure Setup (RDS, Secrets Manager, Elastic Beanstalk setup)
- **Week 2:** Application Deployment & RDS Migration, Monitoring, Documentation

---

## 🏗️ Architecture Overview

```
Internet
  ↓
Application Load Balancer (Auto-created by Elastic Beanstalk)
  ↓
├── Elastic Beanstalk Environment (user-service)
│   └── Spring Boot JAR on EC2 (managed by EB)
├── Elastic Beanstalk Environment (task-service)
│   └── Spring Boot JAR on EC2 (managed by EB)
  ↓
├── Amazon RDS PostgreSQL (user-db)
└── Amazon RDS PostgreSQL (task-db)
  ↓
AWS Secrets Manager (JWT secrets, DB passwords)
  ↓
CloudWatch (Monitoring & Logs)
```

---

## Phase 1: AWS Account Setup & Planning (Day 1)

### Objectives:
- Set up AWS account and configure access
- Plan infrastructure architecture
- Set up development environment

### Tasks:

#### 1.1 AWS Account Setup
- [ ] Create AWS account (if not exists)
- [ ] Enable MFA on root account
- [ ] Create IAM user for programmatic access
- [ ] Configure AWS CLI: `aws configure`
- [ ] Test AWS CLI: `aws sts get-caller-identity`

#### 1.2 Plan Infrastructure
- [ ] Choose AWS region (e.g., `us-east-1`, `eu-west-1`)
- [ ] Document architecture diagram
- [ ] List all required resources:
  - 2x RDS PostgreSQL instances
  - 2x Elastic Beanstalk environments (user-service, task-service)
  - 2x Application Load Balancers (auto-created by Elastic Beanstalk)
  - VPC and networking (auto-created or custom)
  - Security groups
  - IAM roles
  - Secrets Manager secrets

#### 1.3 Set Up Local Environment
- [ ] Install Terraform (if using IaC) OR use AWS Console
- [ ] Install Elastic Beanstalk CLI (optional, for easier deployment):
  ```bash
  # macOS
  brew install aws-elasticbeanstalk
  
  # Or use pip
  pip install awsebcli
  ```
- [ ] Create project folder structure:
  ```
  aws-springboot-todo-app/
  ├── backend/
  ├── terraform/          # Infrastructure as Code (optional)
  └── docs/
  ```

**Deliverables:**
- ✅ AWS account configured
- ✅ AWS CLI working
- ✅ Project structure ready

---

## Phase 2: Build Spring Boot Applications (Day 2)

### Objectives:
- Build JAR files for both services
- Prepare for deployment

### Tasks:

#### 2.1 Build User Service
```bash
cd backend/user-service
./mvnw clean package -DskipTests
```
- [ ] Verify JAR created: `target/user-service-0.0.1-SNAPSHOT.jar`
- [ ] Test JAR locally (optional):
  ```bash
  java -jar target/user-service-0.0.1-SNAPSHOT.jar
  ```

#### 2.2 Build Task Service
```bash
cd backend/task-service
./mvnw clean package -DskipTests
```
- [ ] Verify JAR created: `target/task-service-0.0.1-SNAPSHOT.jar`
- [ ] Test JAR locally (optional)

**Deliverables:**
- ✅ JAR files built for both services
- ✅ JARs tested locally (optional)

---

## Phase 3: Infrastructure Setup - RDS (Day 3-4)

### Objectives:
- Create RDS PostgreSQL instances
- Configure security
- Set up Secrets Manager

### Tasks:

#### 3.1 Create RDS Instances (AWS Console or Terraform)

**Option A: AWS Console**
1. Go to RDS Console
2. Create PostgreSQL database:
   - Engine: PostgreSQL 16
   - Template: Free tier (or Production)
   - Instance identifier: `user-db`
   - Master username: `admin` (or your choice)
   - Master password: (store in Secrets Manager)
   - Instance class: `db.t3.micro` (free tier) or `db.t3.small`
   - Storage: 20GB
   - VPC: Default or create new
   - Public access: No (for security)
   - Database name: `userdb`

3. Repeat for `task-db` with database name: `taskdb`

**Option B: Terraform**
- [ ] Create Terraform configuration for RDS
- [ ] Define both RDS instances
- [ ] Apply configuration

#### 3.2 Configure Security Groups
- [ ] Security group for RDS:
  - Allow inbound: PostgreSQL (5432) from Elastic Beanstalk security group
  - Source: Elastic Beanstalk security group

#### 3.3 Set Up Secrets Manager
- [ ] Create secret for RDS credentials:
  - Name: `rds-credentials`
  - Content: `{"username":"admin","password":"your-password"}`
- [ ] Create secret for JWT secret:
  - Name: `jwt-secret`
  - Content: `{"secret":"WGhzVGZqNXdKSmJvcG1lT1BoU3pBRFNlcnZKeWNYR1c="}`
- [ ] Create secret for user-db endpoint:
  - Name: `rds-user-db-endpoint`
  - Content: `{"endpoint":"your-rds-endpoint.rds.amazonaws.com"}`
- [ ] Create secret for task-db endpoint:
  - Name: `rds-task-db-endpoint`
  - Content: `{"endpoint":"your-rds-endpoint.rds.amazonaws.com"}`

#### 3.4 Test RDS Connectivity
- [ ] Temporarily enable public access (for testing) OR use bastion
- [ ] Connect to RDS and create databases:
  ```bash
  psql -h <rds-endpoint> -U admin -d postgres
  CREATE DATABASE userdb;
  CREATE DATABASE taskdb;
  \q
  ```
- [ ] Disable public access after testing (for security)

**Deliverables:**
- ✅ RDS instances created
- ✅ Databases created
- ✅ Secrets Manager configured
- ✅ Security groups configured

---

## Phase 4: Deploy to Elastic Beanstalk (Day 5-7)

### Objectives:
- Create Elastic Beanstalk environments
- Deploy Spring Boot JARs
- Configure environment variables

### Tasks:

#### 4.1 Create Elastic Beanstalk Application
- [ ] Go to Elastic Beanstalk Console
- [ ] Create new application:
  - Application name: `todo-app`
  - Description: "Todo Application Microservices"

#### 4.2 Create User Service Environment

**Using AWS Console:**

1. **Create Environment:**
   - Environment name: `user-service-env`
   - Domain: (auto-generated or custom)
   - Platform: `Java`
   - Platform branch: `Java 17 running on 64bit Amazon Linux 2023`
   - Platform version: Latest
   - Application code: Upload JAR file
   - Upload: `backend/user-service/target/user-service-0.0.1-SNAPSHOT.jar`

2. **Configure Environment:**
   - Instance type: `t3.micro` (free tier) or `t3.small`
   - Key pair: (optional, for SSH access)
   - VPC: Default or select your VPC
   - Subnets: Select public subnets

3. **Configure Environment Variables:**
   - Add environment properties:
     ```
     SERVER_PORT=8081
     SPRING_PROFILES_ACTIVE=aws
     DB_HOST=${aws:secretsmanager:rds-user-db-endpoint:endpoint::}
     DB_USERNAME=${aws:secretsmanager:rds-credentials:username::}
     DB_PASSWORD=${aws:secretsmanager:rds-credentials:password::}
     JWT_SECRET=${aws:secretsmanager:jwt-secret:secret::}
     ```
   
   **Note:** Elastic Beanstalk doesn't directly support Secrets Manager references in environment variables. You'll need to:
   - Either use Systems Manager Parameter Store (SSM)
   - Or manually copy values from Secrets Manager
   - Or create a startup script to fetch from Secrets Manager

4. **Configure Capacity:**
   - Environment type: Single instance (for cost) or Load balanced
   - Instance count: 1 (or 2+ for high availability)

5. **Create Environment** and wait for deployment

**Using EB CLI (Alternative):**

```bash
# Initialize EB
cd backend/user-service
eb init

# Create environment
eb create user-service-env \
  --instance-type t3.micro \
  --platform "Java 17 running on 64bit Amazon Linux 2023"

# Set environment variables
eb setenv SERVER_PORT=8081 \
  SPRING_PROFILES_ACTIVE=aws \
  DB_HOST=<rds-endpoint> \
  DB_USERNAME=<username> \
  DB_PASSWORD=<password> \
  JWT_SECRET=<jwt-secret>

# Deploy
eb deploy
```

#### 4.3 Create Task Service Environment

**Repeat same process for task-service:**
- [ ] Environment name: `task-service-env`
- [ ] Platform: Java 17
- [ ] Upload: `backend/task-service/target/task-service-0.0.1-SNAPSHOT.jar`
- [ ] Environment variables:
  ```
  SERVER_PORT=8082
  SPRING_PROFILES_ACTIVE=aws
  DB_HOST=${aws:secretsmanager:rds-task-db-endpoint:endpoint::}
  DB_USERNAME=${aws:secretsmanager:rds-credentials:username::}
  DB_PASSWORD=${aws:secretsmanager:rds-credentials:password::}
  JWT_SECRET=${aws:secretsmanager:jwt-secret:secret::}
  USER_SERVICE_URL=http://user-service-env.region.elasticbeanstalk.com
  ```

#### 4.4 Update Spring Boot Configuration

**Create `application-aws.properties` for both services:**

`backend/user-service/src/main/resources/application-aws.properties`:
```properties
spring.application.name=user-service
server.port=${SERVER_PORT:8081}

# RDS Configuration (from environment variables)
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/userdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT Configuration (from environment variables)
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Logging
logging.level.root=INFO
logging.level.com.microtodo=DEBUG
```

`backend/task-service/src/main/resources/application-aws.properties`:
```properties
spring.application.name=task-service
server.port=${SERVER_PORT:8082}

# RDS Configuration
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/taskdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# User Service Configuration
user.service.url=${USER_SERVICE_URL:http://user-service-env.region.elasticbeanstalk.com}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Logging
logging.level.root=INFO
logging.level.com.microtodo=DEBUG
```

#### 4.5 Rebuild and Redeploy

After updating config files:
```bash
# User Service
cd backend/user-service
./mvnw clean package -DskipTests
# Upload new JAR to Elastic Beanstalk

# Task Service
cd backend/task-service
./mvnw clean package -DskipTests
# Upload new JAR to Elastic Beanstalk
```

#### 4.6 Verify Deployment
- [ ] Check Elastic Beanstalk environment health (green status)
- [ ] Check CloudWatch logs for errors
- [ ] Test endpoints:
  - User Service: `http://user-service-env.region.elasticbeanstalk.com/api/users/check-username?username=test`
  - Task Service: `http://task-service-env.region.elasticbeanstalk.com/actuator/health`

**Deliverables:**
- ✅ Both services deployed to Elastic Beanstalk
- ✅ Environments healthy
- ✅ Endpoints accessible

---

## Phase 5: Database Migration (Day 8-9)

### Objectives:
- Migrate local databases to RDS
- Verify data integrity
- Test all operations

### Tasks:

#### 5.1 Export Local Databases
```bash
# Export userdb
pg_dump -h localhost -U sarptekin -d userdb > userdb_backup.sql

# Export taskdb
pg_dump -h localhost -U sarptekin -d taskdb > taskdb_backup.sql
```

#### 5.2 Import to RDS

**Option 1: Temporarily Enable Public Access**
- [ ] Modify RDS security group to allow your IP temporarily
- [ ] Connect and import:
  ```bash
  psql -h <rds-endpoint> -U admin -d userdb < userdb_backup.sql
  psql -h <rds-endpoint> -U admin -d taskdb < taskdb_backup.sql
  ```
- [ ] Remove public access after import

**Option 2: Use EC2 Bastion (More Secure)**
- [ ] Launch EC2 instance in same VPC as RDS
- [ ] SSH to EC2
- [ ] Install PostgreSQL client
- [ ] Import data from EC2

#### 5.3 Verify Migration
- [ ] Connect to RDS and verify data
- [ ] Test login with migrated users
- [ ] Test task operations via Elastic Beanstalk URLs

#### 5.4 Update Applications
- [ ] Restart Elastic Beanstalk environments if needed
- [ ] Test all endpoints work with RDS data
- [ ] Verify performance

**Deliverables:**
- ✅ Data migrated to RDS
- ✅ All operations working
- ✅ Performance verified

---

## Phase 6: Application Load Balancer Configuration (Day 10)

### Objectives:
- Configure ALB for both services
- Set up path-based routing
- Enable HTTPS

### Tasks:

#### 6.1 Elastic Beanstalk ALB (Automatic)

**Note:** Elastic Beanstalk creates its own ALB for each environment. You have two options:

**Option A: Keep Separate ALBs (Simpler)**
- Each service has its own ALB
- User Service URL: `http://user-service-env.region.elasticbeanstalk.com`
- Task Service URL: `http://task-service-env.region.elasticbeanstalk.com`
- Frontend calls appropriate service directly

**Option B: Custom ALB with Path Routing (Advanced)**
- Create separate ALB in front of both EB environments
- Configure path routing:
  - `/api/users/**` → user-service EB
  - `/api/tasks/**` → task-service EB
- Requires Elastic Beanstalk integration with external ALB

#### 6.2 SSL/TLS Certificates
- [ ] Request certificate in AWS Certificate Manager (ACM)
- [ ] Verify domain ownership (if using custom domain)
- [ ] Configure HTTPS listener in Elastic Beanstalk
- [ ] Enable HTTP to HTTPS redirect

**Deliverables:**
- ✅ ALB configured
- ✅ HTTPS enabled
- ✅ Path routing working (if Option B)

---

## Phase 7: Monitoring & Optimization (Day 11-12)

### Objectives:
- Set up CloudWatch monitoring
- Configure alarms
- Optimize performance
- Set up logging

### Tasks:

#### 7.1 CloudWatch Dashboards
- [ ] Create dashboard for RDS:
  - CPU utilization
  - Database connections
  - Read/Write IOPS
  - Free storage space
- [ ] Create dashboard for Elastic Beanstalk:
  - Request count
  - Response time
  - Error rate (4xx, 5xx)
  - CPU utilization
  - Memory utilization

#### 7.2 CloudWatch Alarms
- [ ] RDS alarms:
  - High CPU (>80%)
  - Low storage (<20% free)
  - High connection count
- [ ] Elastic Beanstalk alarms:
  - High CPU (>80%)
  - High memory (>80%)
  - Error rate > 5%
  - Response time > 2 seconds

#### 7.3 CloudWatch Logs
- [ ] Elastic Beanstalk automatically sends logs to CloudWatch
- [ ] Verify logs are being collected
- [ ] Create log insights queries for:
  - Error patterns
  - Slow requests
  - Failed authentication attempts

#### 7.4 Performance Optimization
- [ ] Review RDS performance:
  - Check slow query log
  - Optimize indexes if needed
  - Review connection pooling settings
- [ ] Review Elastic Beanstalk:
  - Adjust instance type if needed
  - Enable auto-scaling if traffic increases
  - Optimize JVM settings

**Deliverables:**
- ✅ Dashboards created
- ✅ Alarms configured
- ✅ Logs collected
- ✅ Performance optimized

---

## Phase 8: Security Hardening (Day 13)

### Objectives:
- Review and harden security
- Ensure HTTPS only
- Implement security best practices

### Tasks:

#### 8.1 Security Group Review
- [ ] Review all security groups
- [ ] Ensure least privilege access
- [ ] Remove unnecessary open ports
- [ ] Verify RDS is only accessible from Elastic Beanstalk

#### 8.2 Secrets Manager Review
- [ ] Verify all secrets are encrypted
- [ ] Review IAM permissions for secrets
- [ ] Consider automatic rotation (optional)

#### 8.3 Network Security
- [ ] Verify VPC is properly isolated
- [ ] Ensure RDS is in private subnets
- [ ] Ensure Elastic Beanstalk instances have minimal access

#### 8.4 IAM Roles Review
- [ ] Review Elastic Beanstalk instance profile
- [ ] Ensure least privilege access
- [ ] Review permissions for Secrets Manager access

**Deliverables:**
- ✅ Security reviewed
- ✅ Best practices implemented
- ✅ HTTPS enforced

---

## Phase 9: Testing & Documentation (Day 14)

### Objectives:
- Comprehensive testing
- Update documentation
- Prepare portfolio materials

### Tasks:

#### 9.1 End-to-End Testing
- [ ] Test all user-service endpoints via EB URL
- [ ] Test all task-service endpoints via EB URL
- [ ] Test authentication flow
- [ ] Test authorization (user isolation)
- [ ] Test error scenarios

#### 9.2 Load Testing (Optional)
- [ ] Use Apache JMeter or similar
- [ ] Test concurrent users (50-100)
- [ ] Monitor RDS performance
- [ ] Monitor Elastic Beanstalk auto-scaling
- [ ] Document results

#### 9.3 Documentation
- [ ] Update README.md with AWS deployment info
- [ ] Create ARCHITECTURE.md:
  - System architecture
  - Component descriptions
  - Data flow diagrams
- [ ] Create DEPLOYMENT.md:
  - Step-by-step deployment guide
  - Environment variable reference
  - Troubleshooting guide

#### 9.4 Portfolio Materials
- [ ] Create architecture diagram
- [ ] Write project description
- [ ] Prepare screenshots (AWS Console, CloudWatch)
- [ ] Update CV/resume

**Deliverables:**
- ✅ All tests passing
- ✅ Documentation complete
- ✅ Portfolio ready

---

## 📊 Success Criteria Checklist

### Infrastructure
- [ ] RDS instances running and accessible
- [ ] Elastic Beanstalk environments healthy
- [ ] Endpoints accessible via EB URLs
- [ ] Security groups configured properly
- [ ] Secrets Manager working

### Application
- [ ] User-service deployed and working
- [ ] Task-service deployed and working
- [ ] Database migration successful
- [ ] All endpoints accessible
- [ ] Authentication working
- [ ] All CRUD operations working

### Monitoring
- [ ] CloudWatch dashboards created
- [ ] Alarms configured
- [ ] Logs being collected
- [ ] Performance acceptable

### Documentation
- [ ] Architecture documented
- [ ] Deployment guide created
- [ ] Portfolio materials ready
- [ ] CV updated

---

## 🚨 Common Issues & Troubleshooting

### Issue: Elastic Beanstalk Deployment Fails
**Solution:** 
- Check CloudWatch logs in EB console
- Verify JAR file is correct
- Check environment variables are set correctly
- Verify security groups allow traffic

### Issue: RDS Connection Timeout
**Solution:** 
- Check security groups (allow 5432 from EB security group)
- Verify RDS endpoint is correct
- Check VPC configuration
- Verify credentials in Secrets Manager

### Issue: Environment Variables Not Working
**Solution:**
- Elastic Beanstalk environment variables are case-sensitive
- Restart environment after changing variables
- Check CloudWatch logs for errors
- Use Systems Manager Parameter Store for sensitive values

### Issue: High Costs
**Solution:**
- Stop Elastic Beanstalk environments when not in use
- Stop RDS instances when not in use (can save ~$30-40/month)
- Use smaller instance types (t3.micro for free tier)
- Scale down during low usage periods

---

## 💰 Cost Estimation (Monthly)

### Core Infrastructure:

| Service | Configuration | Monthly Cost |
|---------|--------------|--------------|
| **RDS PostgreSQL** | 2x db.t3.micro | ~$30-40 |
| **Elastic Beanstalk** | 2x t3.micro instances | ~$15-20 |
| **Application Load Balancer** | 2x ALB (auto-created) | ~$30-40 |
| **Secrets Manager** | 4 secrets | ~$1.60 |
| **CloudWatch** | Logs + metrics | ~$5-10 |
| **Data Transfer** | Minimal | ~$1-5 |
| **Total** | | **~$80-115/month** |

**With Free Tier (first 12 months):** ~$50-80/month

**Cost Optimization:**
- Stop Elastic Beanstalk when not in use: Save ~$15-20/month
- Stop RDS when not in use: Save ~$30-40/month
- **Optimized Total:** ~$5-25/month (when stopped)

---

## 📈 Portfolio Value Summary

### **What This Shows:**
- ✅ Microservices architecture on AWS
- ✅ Managed deployment with Elastic Beanstalk
- ✅ Managed database administration (RDS)
- ✅ Load balancing and high availability
- ✅ Infrastructure as Code (Terraform - optional)
- ✅ Security best practices (Secrets Manager)
- ✅ Monitoring and observability (CloudWatch)
- ✅ Production-ready deployment

### **CV Bullet Points:**
- "Deployed Spring Boot microservices on AWS Elastic Beanstalk"
- "Migrated PostgreSQL to Amazon RDS with automated backups"
- "Configured high availability with Application Load Balancer"
- "Integrated AWS Secrets Manager for secure credential management"
- "Implemented comprehensive monitoring with CloudWatch"

---

## 🎯 Key Advantages of Elastic Beanstalk

1. **No Docker Required:** Deploys JAR files directly
2. **Easy Deployment:** Upload JAR, EB handles the rest
3. **Auto-scaling:** Built-in auto-scaling support
4. **Health Monitoring:** Automatic health checks
5. **Managed Platform:** AWS handles OS updates, patches
6. **Load Balancer:** ALB created automatically
7. **CloudWatch Integration:** Logs sent automatically

---

## ✅ Final Checklist

- [ ] All infrastructure deployed
- [ ] Both services deployed to Elastic Beanstalk
- [ ] RDS migration complete
- [ ] CloudWatch monitoring active
- [ ] HTTPS enabled
- [ ] Documentation complete
- [ ] Portfolio materials ready
- [ ] Costs reviewed and optimized

**🎉 Congratulations! Your AWS deployment is complete!**

---

## 📚 Resources & References

### AWS Documentation
- [Elastic Beanstalk Java Platform](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html)
- [RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [Secrets Manager](https://docs.aws.amazon.com/secretsmanager/latest/userguide/intro.html)

### Tools
- [AWS CLI Reference](https://docs.aws.amazon.com/cli/latest/reference/)
- [Elastic Beanstalk CLI](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html)
- [CloudWatch Dashboards](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Dashboards.html)

---

**Good luck with your deployment! 🚀**
