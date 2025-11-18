# AWS Deployment Roadmap - Simplified (No Lambda)

**Goal:** Deploy Spring Boot microservices to AWS with RDS and core infrastructure.

---

## 📋 Prerequisites Checklist

Before starting, ensure you have:
- [ ] AWS Account created (use free tier where possible)
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Terraform installed (recommended) OR AWS Console access
- [ ] AWS credentials configured with appropriate permissions
- [ ] Spring Boot applications tested locally

**Required AWS Permissions:**
- RDS (create, modify, delete instances)
- ECS (create clusters, services, task definitions)
- Secrets Manager (create, read secrets)
- VPC, Security Groups, IAM (create resources)
- CloudWatch (create log groups)
- Application Load Balancer (create, configure)

---

## 🗓️ Timeline Overview

**Total Estimated Time: 2-3 weeks (working part-time)**

- **Week 1:** Infrastructure Setup (RDS, ECS, Secrets Manager, ALB)
- **Week 2:** Application Deployment & RDS Migration
- **Week 3:** Monitoring, Testing, Documentation

---

## Phase 1: AWS Account Setup & Planning (Day 1-2)

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
  - 1x ECS cluster
  - 2x ECS services (user-service, task-service)
  - 1x Application Load Balancer
  - 1x VPC with subnets
  - Security groups
  - IAM roles
  - Secrets Manager secrets

#### 1.3 Set Up Local Environment
- [ ] Install Terraform (if using IaC)
- [ ] Create project folder structure:
  ```
  aws-springboot-todo-app/
  ├── backend/
  ├── terraform/          # Infrastructure as Code
  │   ├── modules/
  │   ├── main.tf
  │   ├── variables.tf
  │   └── outputs.tf
  └── docs/
  ```

**Deliverables:**
- ✅ AWS account configured
- ✅ AWS CLI working
- ✅ Project structure ready

---

## Phase 2: Infrastructure as Code (Terraform) (Day 3-5)

### Objectives:
- Define all AWS resources using Terraform
- Create VPC, networking, security groups
- Set up RDS instances
- Prepare ECS infrastructure

### Tasks:

#### 2.1 Create Terraform Configuration
- [ ] Create `terraform/main.tf` with provider configuration
- [ ] Create `terraform/variables.tf` with:
  - AWS region
  - Project name
  - Environment (dev/prod)
  - Database credentials (use variables)
  - VPC CIDR blocks

#### 2.2 VPC and Networking
- [ ] Create VPC module
- [ ] Create public/private subnets (2 AZs minimum)
- [ ] Create Internet Gateway
- [ ] Create NAT Gateway (for private subnets)
- [ ] Create route tables
- [ ] Create VPC endpoints (for S3, ECR - optional but recommended)

#### 2.3 Security Groups
- [ ] Security group for RDS (allow 5432 from ECS)
- [ ] Security group for ECS (allow 8081, 8082 from ALB)
- [ ] Security group for ALB (allow 80, 443 from internet)

#### 2.4 RDS Configuration
- [ ] Create RDS subnet group
- [ ] Create RDS parameter group (if custom settings needed)
- [ ] Define `user-db` RDS instance:
  - Engine: PostgreSQL 16
  - Instance: db.t3.micro (free tier) or db.t3.small
  - Storage: 20GB (auto-scaling enabled)
  - Multi-AZ: false (for cost) or true (for production)
  - Backup: 7 days retention
  - Publicly accessible: false
- [ ] Define `task-db` RDS instance (same config)
- [ ] Store DB credentials in Secrets Manager

#### 2.5 ECS Infrastructure
- [ ] Create ECS cluster
- [ ] Create ECS task execution role (for pulling images, Secrets Manager)
- [ ] Create ECS task role (for application permissions)
- [ ] Create CloudWatch log groups for services

#### 2.6 Application Load Balancer
- [ ] Create ALB
- [ ] Create target groups:
  - `user-service-tg` (port 8081)
  - `task-service-tg` (port 8082)
- [ ] Create ALB listener (port 80) and rules:
  - `/api/users/**` → user-service-tg
  - `/api/tasks/**` → task-service-tg
  - `/api/auth/**` → user-service-tg

#### 2.7 Secrets Manager
- [ ] Create secret for RDS credentials
- [ ] Create secret for JWT secret key
- [ ] Create secret for user-db connection string
- [ ] Create secret for task-db connection string

**Deliverables:**
- ✅ Complete Terraform configuration
- ✅ `terraform plan` runs successfully
- ✅ Review infrastructure plan

**Checkpoint:** Run `terraform plan` and review all resources before creating.

---

## Phase 3: Deploy Infrastructure (Day 6-7)

### Objectives:
- Deploy AWS infrastructure
- Verify all resources created
- Test connectivity

### Tasks:

#### 3.1 Initialize Terraform
```bash
cd terraform
terraform init
terraform validate
terraform plan  # Review output carefully
```

#### 3.2 Deploy Infrastructure
```bash
terraform apply  # Type 'yes' when prompted
```

#### 3.3 Verify Resources
- [ ] Verify VPC created
- [ ] Verify RDS instances created and available
- [ ] Verify ECS cluster created
- [ ] Verify ALB created (note DNS name)
- [ ] Verify security groups created
- [ ] Verify Secrets Manager secrets created

#### 3.4 Test RDS Connectivity
- [ ] Connect to RDS from local machine (using bastion or temporarily public)
- [ ] Create databases: `userdb` and `taskdb`
- [ ] Test connection with psql:
  ```bash
  psql -h <rds-endpoint> -U <username> -d postgres
  ```

#### 3.5 Store Connection Info
- [ ] Document RDS endpoints
- [ ] Document ALB DNS name
- [ ] Document secret ARNs
- [ ] Save Terraform outputs:
  ```bash
  terraform output > ../docs/terraform-outputs.txt
  ```

**Deliverables:**
- ✅ All infrastructure deployed
- ✅ RDS instances accessible
- ✅ Resources documented

---

## Phase 4: Containerize Applications (Day 8-9)

### Objectives:
- Build Docker images for both services
- Push images to ECR
- Update Spring Boot configs for AWS

### Tasks:

#### 4.1 Prepare Docker Images

**Note:** Since we removed Docker from local development, we'll create Dockerfiles specifically for AWS deployment.

**Create `backend/user-service/Dockerfile`:**
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B || true
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Create `backend/task-service/Dockerfile`:**
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B || true
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] Create Dockerfiles for both services
- [ ] Build images locally and test:
  ```bash
  cd backend/user-service
  docker build -t user-service:latest .
  docker run -p 8081:8081 user-service:latest  # Test locally
  
  cd ../task-service
  docker build -t task-service:latest .
  docker run -p 8082:8082 task-service:latest  # Test locally
  ```

#### 4.2 Create ECR Repositories
- [ ] Create ECR repository for user-service:
  ```bash
  aws ecr create-repository --repository-name user-service
  ```
- [ ] Create ECR repository for task-service:
  ```bash
  aws ecr create-repository --repository-name task-service
  ```
- [ ] Get login token:
  ```bash
  aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com
  ```

#### 4.3 Push Images to ECR
- [ ] Tag and push user-service:
  ```bash
  docker tag user-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/user-service:latest
  docker push <account-id>.dkr.ecr.<region>.amazonaws.com/user-service:latest
  ```
- [ ] Tag and push task-service:
  ```bash
  docker tag task-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/task-service:latest
  docker push <account-id>.dkr.ecr.<region>.amazonaws.com/task-service:latest
  ```

#### 4.4 Update Spring Boot Configuration
- [ ] Create `application-aws.properties` files for both services
- [ ] Configure RDS connection (use Secrets Manager references)
- [ ] Configure JWT secret (from Secrets Manager)
- [ ] Update CORS to allow ALB domain (if needed for web UI)
- [ ] Disable local database auto-config if needed

**Example:** `backend/user-service/src/main/resources/application-aws.properties`
```properties
# RDS Configuration (will be overridden by ECS task definition env vars)
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/userdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Server Configuration
server.port=8081
spring.profiles.active=aws
```

**Deliverables:**
- ✅ Docker images in ECR
- ✅ Spring Boot configs updated for AWS
- ✅ Images tested locally

---

## Phase 5: Deploy to ECS (Day 10-12)

### Objectives:
- Create ECS task definitions
- Deploy services to ECS
- Configure ALB routing
- Test endpoints

### Tasks:

#### 5.1 Create ECS Task Definitions
- [ ] Create task definition for user-service:
  - Image: ECR image URI
  - CPU: 256 (0.25 vCPU)
  - Memory: 512 MB
  - Port mappings: 8081
  - Environment variables: DB_HOST, DB_USERNAME, DB_PASSWORD (from Secrets Manager)
  - JWT_SECRET (from Secrets Manager)
  - Logging: CloudWatch logs
- [ ] Create task definition for task-service (similar config, port 8082)

**Example Task Definition JSON:**
```json
{
  "family": "user-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [{
    "name": "user-service",
    "image": "<account-id>.dkr.ecr.<region>.amazonaws.com/user-service:latest",
    "portMappings": [{
      "containerPort": 8081,
      "protocol": "tcp"
    }],
    "environment": [
      {"name": "SPRING_PROFILES_ACTIVE", "value": "aws"}
    ],
    "secrets": [
      {
        "name": "DB_HOST",
        "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:rds-user-db-host"
      },
      {
        "name": "DB_USERNAME",
        "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:rds-credentials:username"
      },
      {
        "name": "DB_PASSWORD",
        "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:rds-credentials:password"
      },
      {
        "name": "JWT_SECRET",
        "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:jwt-secret"
      }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/user-service",
        "awslogs-region": "<region>",
        "awslogs-stream-prefix": "ecs"
      }
    }
  }]
}
```

#### 5.2 Create ECS Services
- [ ] Create user-service:
  - Cluster: your ECS cluster
  - Task definition: user-service
  - Desired count: 1 (increase later for HA)
  - Launch type: Fargate
  - VPC: your VPC
  - Subnets: private subnets
  - Security group: ECS security group
  - Load balancer: ALB, target group: user-service-tg
  - Health check: `/actuator/health`
- [ ] Create task-service (similar config)

#### 5.3 Verify Deployment
- [ ] Check ECS services are running
- [ ] Check tasks are healthy
- [ ] Check ALB target health
- [ ] Check CloudWatch logs for errors

#### 5.4 Test Endpoints
- [ ] Test ALB endpoint: `http://<alb-dns>/api/users/check-username?username=test`
- [ ] Test registration: `POST http://<alb-dns>/api/users`
- [ ] Test login: `POST http://<alb-dns>/api/auth/login`
- [ ] Test task endpoints: `GET http://<alb-dns>/api/tasks`

**Deliverables:**
- ✅ Services running on ECS
- ✅ ALB routing working
- ✅ Endpoints accessible

---

## Phase 6: Database Migration (Day 13-14)

### Objectives:
- Migrate local databases to RDS
- Verify data integrity
- Test all operations

### Tasks:

#### 6.1 Export Local Databases
```bash
# Export userdb
pg_dump -h localhost -U sarptekin -d userdb > userdb_backup.sql

# Export taskdb
pg_dump -h localhost -U sarptekin -d taskdb > taskdb_backup.sql
```

#### 6.2 Create Databases in RDS
- [ ] Connect to RDS user-db instance
- [ ] Create database: `CREATE DATABASE userdb;`
- [ ] Connect to RDS task-db instance
- [ ] Create database: `CREATE DATABASE taskdb;`

#### 6.3 Import Data
```bash
# Import userdb
psql -h <user-rds-endpoint> -U <username> -d userdb < userdb_backup.sql

# Import taskdb
psql -h <task-rds-endpoint> -U <username> -d taskdb < taskdb_backup.sql
```

#### 6.4 Verify Migration
- [ ] Verify user count matches
- [ ] Verify task count matches
- [ ] Test login with migrated users
- [ ] Test task operations

#### 6.5 Update Application
- [ ] Restart ECS services if needed
- [ ] Test all endpoints work with RDS data
- [ ] Verify performance is acceptable

**Deliverables:**
- ✅ Data migrated to RDS
- ✅ All operations working
- ✅ Performance verified

---

## Phase 7: Monitoring & Optimization (Day 15-17)

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
- [ ] Create dashboard for ECS:
  - CPU utilization
  - Memory utilization
  - Task count
  - Request count
- [ ] Create dashboard for ALB:
  - Request count
  - Response time
  - Error rates (4xx, 5xx)
  - Target response time

#### 7.2 CloudWatch Alarms
- [ ] RDS alarms:
  - High CPU (>80%)
  - Low storage (<20% free)
  - High connection count
- [ ] ECS alarms:
  - High CPU (>80%)
  - High memory (>80%)
  - Task failed/stopped
- [ ] ALB alarms:
  - Error rate > 5%
  - Response time > 2 seconds

#### 7.3 CloudWatch Logs
- [ ] Verify logs are being collected:
  - ECS service logs
  - ALB access logs (enable if needed)
- [ ] Create log insights queries for:
  - Error patterns
  - Slow requests
  - Failed authentication attempts

#### 7.4 Performance Optimization
- [ ] Review RDS performance:
  - Check slow query log
  - Optimize indexes if needed
  - Review connection pooling settings
- [ ] Review ECS performance:
  - Adjust CPU/memory if needed
  - Consider auto-scaling
- [ ] Review ALB:
  - Enable sticky sessions if needed
  - Optimize health checks

**Deliverables:**
- ✅ Dashboards created
- ✅ Alarms configured
- ✅ Logs collected
- ✅ Performance optimized

---

## Phase 8: Security Hardening (Day 18)

### Objectives:
- Review and harden security
- Enable HTTPS
- Implement security best practices

### Tasks:

#### 8.1 SSL/TLS Certificate
- [ ] Request certificate in AWS Certificate Manager (ACM)
- [ ] Verify domain ownership
- [ ] Update ALB listener to use HTTPS (port 443)
- [ ] Configure HTTP to HTTPS redirect

#### 8.2 Security Group Review
- [ ] Review all security groups
- [ ] Ensure least privilege access
- [ ] Remove unnecessary open ports
- [ ] Verify RDS is only accessible from ECS

#### 8.3 Secrets Manager Review
- [ ] Verify all secrets are encrypted
- [ ] Review IAM permissions for secrets
- [ ] Enable automatic rotation (optional)

#### 8.4 Network Security
- [ ] Verify VPC is properly isolated
- [ ] Review NACLs (if configured)
- [ ] Ensure RDS is in private subnets
- [ ] Ensure ECS tasks are in private subnets

#### 8.5 IAM Roles Review
- [ ] Review ECS task execution role
- [ ] Review ECS task role
- [ ] Ensure least privilege access

**Deliverables:**
- ✅ HTTPS enabled
- ✅ Security reviewed
- ✅ Best practices implemented

---

## Phase 9: Testing & Documentation (Day 19-20)

### Objectives:
- Comprehensive testing
- Update documentation
- Prepare portfolio materials

### Tasks:

#### 9.1 End-to-End Testing
- [ ] Test all user-service endpoints:
  - Registration
  - Login
  - Profile management
  - Password change
  - Account deletion
- [ ] Test all task-service endpoints:
  - Create task
  - List tasks
  - Update task
  - Delete task
  - Filter by status
- [ ] Test error scenarios:
  - Invalid credentials
  - Expired tokens
  - Invalid requests
  - Network failures

#### 9.2 Load Testing (Optional)
- [ ] Use Apache JMeter or similar
- [ ] Test concurrent users (50-100)
- [ ] Monitor RDS performance
- [ ] Monitor ECS scaling
- [ ] Document results

#### 9.3 Documentation
- [ ] Update README.md with:
  - AWS architecture diagram
  - Deployment instructions
  - Environment variables
  - Troubleshooting guide
- [ ] Create ARCHITECTURE.md:
  - System architecture
  - Component descriptions
  - Data flow diagrams
  - Security architecture
- [ ] Create DEPLOYMENT.md:
  - Step-by-step deployment guide
  - Rollback procedures
  - Update procedures

#### 9.4 Portfolio Materials
- [ ] Create architecture diagram (Lucidchart, Draw.io)
- [ ] Write project description:
  - Technologies used
  - Challenges overcome
  - Key achievements
- [ ] Prepare screenshots:
  - AWS Console (resources)
  - CloudWatch dashboards
  - Architecture diagrams
- [ ] Update CV/resume with:
  - AWS technologies
  - Specific achievements
  - Key metrics (if available)

**Deliverables:**
- ✅ All tests passing
- ✅ Documentation complete
- ✅ Portfolio ready

---

## Phase 10: Cleanup & Cost Optimization (Day 21)

### Objectives:
- Review costs
- Optimize resources
- Document cost breakdown

### Tasks:

#### 10.1 Cost Review
- [ ] Review AWS Cost Explorer
- [ ] Identify expensive resources
- [ ] Check for unused resources
- [ ] Review free tier usage

#### 10.2 Cost Optimization
- [ ] RDS:
  - Consider stopping RDS when not in use (dev environment)
  - Use reserved instances if keeping long-term
  - Right-size instance types
- [ ] ECS:
  - Review CPU/memory allocation
  - Scale down when not in use
- [ ] NAT Gateway:
  - Only needed if ECS tasks need internet access
  - Consider removing if not needed (can save ~$30/month)

#### 10.3 Cost Monitoring
- [ ] Set up billing alarms
- [ ] Configure budget alerts
- [ ] Document monthly cost estimate

#### 10.4 Cleanup Script (for testing)
- [ ] Create script to stop/start resources for cost savings
- [ ] Document cleanup procedures

**Deliverables:**
- ✅ Costs optimized
- ✅ Monitoring in place
- ✅ Cost documentation

---

## 📊 Success Criteria Checklist

### Infrastructure
- [ ] RDS instances running and accessible
- [ ] ECS services running and healthy
- [ ] ALB routing correctly
- [ ] Security groups configured properly
- [ ] Secrets Manager working

### Application
- [ ] User-service deployed and working
- [ ] Task-service deployed and working
- [ ] Database migration successful
- [ ] All endpoints accessible via ALB
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

### Issue: RDS Connection Timeout
**Solution:** Check security groups, ensure ECS tasks are in correct VPC/subnet

### Issue: ECS Tasks Failing to Start
**Solution:** Check task execution role, Secrets Manager permissions, image pull permissions

### Issue: High Costs
**Solution:** Stop RDS when not in use, reduce ECS desired count, use smaller instance types

### Issue: ALB Not Routing
**Solution:** Check target group health, verify security groups allow ALB → ECS traffic

---

## 📈 Next Steps (After Completion)

### Optional Enhancements:
1. **Auto-scaling:**
   - Configure ECS auto-scaling based on CPU/memory
   - Implement ALB target scaling

2. **CI/CD Pipeline:**
   - Set up AWS CodePipeline
   - Automated testing and deployment

3. **API Gateway:**
   - Migrate from ALB to API Gateway
   - Add rate limiting, caching

4. **Advanced Monitoring:**
   - AWS X-Ray for distributed tracing
   - Custom CloudWatch metrics

---

## 📝 Weekly Progress Tracking

### Week 1 Progress
- [ ] Infrastructure setup complete
- [ ] Terraform code written
- [ ] Resources deployed

### Week 2 Progress
- [ ] Applications deployed
- [ ] Database migrated
- [ ] Endpoints working

### Week 3 Progress
- [ ] Monitoring configured
- [ ] Documentation complete
- [ ] Portfolio ready

---

## 🎯 Final Checklist Before Marking Complete

- [ ] All infrastructure deployed
- [ ] Both services running on ECS
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
- [ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/intro.html)
- [RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [ALB Documentation](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/introduction.html)

### Tools
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS CLI Reference](https://docs.aws.amazon.com/cli/latest/reference/)
- [CloudWatch Dashboards](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Dashboards.html)

---

## 💰 Cost Estimation (Monthly)

**Core Infrastructure:**
- RDS PostgreSQL (2x db.t3.micro): ~$30-40/month
- ECS Fargate (2 services, 0.5 vCPU each): ~$30-50/month
- ALB: ~$20-25/month
- Secrets Manager: ~$0.40 per secret (~$1.20/month)
- CloudWatch: ~$5-10/month (logs/metrics)
- NAT Gateway (if needed): ~$32/month
- VPC Endpoints (optional): ~$7/month each

**Total Estimated:** ~$120-160/month (without NAT Gateway)  
**With Free Tier:** ~$70-100/month for first 12 months

**Note:** Can significantly reduce by stopping RDS when not in use (~$0-30/month when stopped)

---

**Good luck with your deployment! 🚀**

