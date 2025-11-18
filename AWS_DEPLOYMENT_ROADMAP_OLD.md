# AWS Deployment Roadmap - Todo Application

## 🎯 Goal
Deploy Spring Boot microservices to AWS with RDS and Lambda-based email notifications.

---

## 📋 Prerequisites Checklist

Before starting, ensure you have:
- [ ] AWS Account created (use free tier where possible)
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Terraform installed (recommended) OR AWS Console access
- [ ] Docker installed (for building container images)
- [ ] AWS credentials configured with appropriate permissions
- [ ] Domain name (optional, for SES email verification)
- [ ] Spring Boot applications tested locally

**Required AWS Permissions:**
- RDS (create, modify, delete instances)
- ECS (create clusters, services, task definitions)
- Lambda (create functions, manage triggers)
- SNS (create topics, subscribe)
- SES (verify email addresses, send emails)
- Secrets Manager (create, read secrets)
- VPC, Security Groups, IAM (create resources)
- CloudWatch (create log groups)

---

## 🗓️ Timeline Overview

**Total Estimated Time: 3-4 weeks (working part-time)**

- **Week 1:** Infrastructure Setup (RDS, ECS, Secrets Manager)
- **Week 2:** Application Deployment & RDS Migration
- **Week 3:** Lambda + SNS Integration
- **Week 4:** Testing, Monitoring, Documentation

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
  - SNS topic
  - Lambda function

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
  ├── docker/             # Dockerfiles if needed
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
- [ ] Security group for Lambda (outbound to SES, SNS)

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
- [ ] Review existing Dockerfiles in `backend/user-service/` and `backend/task-service/`
- [ ] Update Dockerfiles if needed (ensure they're production-ready)
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
- [ ] Update CORS to allow ALB domain
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
pg_dump -h localhost -U postgres -d userdb > userdb_backup.sql

# Export taskdb
pg_dump -h localhost -U postgres -d taskdb > taskdb_backup.sql
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

## Phase 7: Lambda + SNS Setup (Day 15-17)

### Objectives:
- Create SNS topic for notifications
- Create Lambda function for email sending
- Integrate Spring Boot with SNS

### Tasks:

#### 7.1 Set Up Amazon SES
- [ ] Verify email address in SES (for sending emails)
  - Go to SES Console → Verified identities
  - Add your email address
  - Click verification link in email
- [ ] Move out of SES sandbox (optional, requires AWS support request):
  - Submit request for production access
  - Or keep in sandbox (can only send to verified emails)

#### 7.2 Create SNS Topic
```bash
aws sns create-topic --name task-notifications
```
- [ ] Note the Topic ARN
- [ ] Create subscription (for testing):
  - Email subscription to your verified email
  - Confirm subscription via email

#### 7.3 Create Lambda Function
- [ ] Create Lambda function:
  - Runtime: Python 3.11 or Java 17 (Kotlin if available)
  - Name: `task-notification-handler`
  - Execution role: Lambda execution role with SES permissions
- [ ] Add Lambda code (see Phase 7.4)
- [ ] Configure environment variables:
  - `SES_REGION`: your AWS region
  - `FROM_EMAIL`: your verified SES email

**Lambda Function Code (Python Example):**
```python
import json
import boto3
import os

ses_client = boto3.client('ses', region_name=os.environ['SES_REGION'])
FROM_EMAIL = os.environ['FROM_EMAIL']

def lambda_handler(event, context):
    try:
        # Parse SNS event
        sns_message = event['Records'][0]['Sns']
        message_body = json.loads(sns_message['Message'])
        
        event_type = message_body.get('eventType')  # 'created', 'completed', etc.
        task_title = message_body.get('taskTitle')
        user_email = message_body.get('userEmail')
        task_id = message_body.get('taskId')
        
        # Email subject and body
        subject = f"Task {event_type.capitalize()}: {task_title}"
        body_text = f"""
        Your task has been {event_type}.
        
        Task: {task_title}
        Task ID: {task_id}
        Status: {event_type}
        
        View your tasks at: <your-app-url>
        """
        
        # Send email via SES
        response = ses_client.send_email(
            Source=FROM_EMAIL,
            Destination={'ToAddresses': [user_email]},
            Message={
                'Subject': {'Data': subject, 'Charset': 'UTF-8'},
                'Body': {
                    'Text': {'Data': body_text, 'Charset': 'UTF-8'}
                }
            }
        )
        
        return {
            'statusCode': 200,
            'body': json.dumps(f'Email sent successfully: {response["MessageId"]}')
        }
        
    except Exception as e:
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error sending email: {str(e)}')
        }
```

#### 7.4 Configure Lambda Trigger
- [ ] Create SNS trigger for Lambda:
  - Topic: task-notifications
  - Lambda: task-notification-handler
- [ ] Test SNS → Lambda:
  - Publish test message to SNS topic
  - Check Lambda logs in CloudWatch
  - Verify email received

#### 7.5 Update Spring Boot Services
- [ ] Add AWS SDK dependency to both services:
  ```xml
  <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-java-sdk-sns</artifactId>
  </dependency>
  ```
- [ ] Create SNS configuration class
- [ ] Create notification service
- [ ] Update TaskService to publish events:
  ```java
  @Service
  public class TaskService {
      @Autowired
      private AmazonSNS snsClient;
      
      @Value("${aws.sns.task-topic-arn}")
      private String taskTopicArn;
      
      public TaskResponse createTask(TaskRequest request) {
          Task task = // ... create task
          
          // Publish to SNS
          Map<String, Object> message = Map.of(
              "eventType", "created",
              "taskTitle", task.getTitle(),
              "userEmail", getCurrentUserEmail(),
              "taskId", task.getId()
          );
          
          PublishRequest publishRequest = new PublishRequest()
              .withTopicArn(taskTopicArn)
              .withMessage(JSON.toJSONString(message));
          
          snsClient.publish(publishRequest);
          
          return convertToResponse(task);
      }
      
      // Similar for updateTask, deleteTask
  }
  ```
- [ ] Update UserService if needed (e.g., welcome email on registration)

#### 7.6 Test End-to-End Flow
- [ ] Create task via API
- [ ] Verify SNS message published
- [ ] Verify Lambda triggered
- [ ] Verify email received
- [ ] Check CloudWatch logs for errors

**Deliverables:**
- ✅ SNS topic created
- ✅ Lambda function working
- ✅ Spring Boot publishing events
- ✅ Emails being sent

---

## Phase 8: Monitoring & Optimization (Day 18-19)

### Objectives:
- Set up CloudWatch monitoring
- Configure alarms
- Optimize performance
- Set up logging

### Tasks:

#### 8.1 CloudWatch Dashboards
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

#### 8.2 CloudWatch Alarms
- [ ] RDS alarms:
  - High CPU (>80%)
  - Low storage (<20% free)
  - High connection count
- [ ] ECS alarms:
  - High CPU (>80%)
  - High memory (>80%)
  - Task failed/stopped
- [ ] Lambda alarms:
  - Error rate > 1%
  - Duration > 5 seconds
- [ ] ALB alarms:
  - Error rate > 5%
  - Response time > 2 seconds

#### 8.3 CloudWatch Logs
- [ ] Verify logs are being collected:
  - ECS service logs
  - Lambda function logs
  - ALB access logs (enable if needed)
- [ ] Create log insights queries for:
  - Error patterns
  - Slow requests
  - Failed authentication attempts

#### 8.4 Performance Optimization
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

## Phase 9: Security Hardening (Day 20)

### Objectives:
- Review and harden security
- Enable HTTPS
- Implement security best practices

### Tasks:

#### 9.1 SSL/TLS Certificate
- [ ] Request certificate in AWS Certificate Manager (ACM)
- [ ] Verify domain ownership
- [ ] Update ALB listener to use HTTPS (port 443)
- [ ] Configure HTTP to HTTPS redirect

#### 9.2 Security Group Review
- [ ] Review all security groups
- [ ] Ensure least privilege access
- [ ] Remove unnecessary open ports
- [ ] Verify RDS is only accessible from ECS

#### 9.3 Secrets Manager Review
- [ ] Verify all secrets are encrypted
- [ ] Review IAM permissions for secrets
- [ ] Enable automatic rotation (optional)

#### 9.4 Network Security
- [ ] Verify VPC is properly isolated
- [ ] Review NACLs (if configured)
- [ ] Ensure RDS is in private subnets
- [ ] Ensure ECS tasks are in private subnets

#### 9.5 IAM Roles Review
- [ ] Review ECS task execution role
- [ ] Review ECS task role
- [ ] Review Lambda execution role
- [ ] Ensure least privilege access

**Deliverables:**
- ✅ HTTPS enabled
- ✅ Security reviewed
- ✅ Best practices implemented

---

## Phase 10: Testing & Documentation (Day 21-22)

### Objectives:
- Comprehensive testing
- Update documentation
- Prepare portfolio materials

### Tasks:

#### 10.1 End-to-End Testing
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
- [ ] Test email notifications:
  - Task created email
  - Task completed email
  - Task updated email
- [ ] Test error scenarios:
  - Invalid credentials
  - Expired tokens
  - Invalid requests
  - Network failures

#### 10.2 Load Testing (Optional)
- [ ] Use Apache JMeter or similar
- [ ] Test concurrent users (50-100)
- [ ] Monitor RDS performance
- [ ] Monitor ECS scaling
- [ ] Document results

#### 10.3 Documentation
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

#### 10.4 Portfolio Materials
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

## Phase 11: Cleanup & Cost Optimization (Day 23)

### Objectives:
- Review costs
- Optimize resources
- Document cost breakdown

### Tasks:

#### 11.1 Cost Review
- [ ] Review AWS Cost Explorer
- [ ] Identify expensive resources
- [ ] Check for unused resources
- [ ] Review free tier usage

#### 11.2 Cost Optimization
- [ ] RDS:
  - Consider stopping RDS when not in use (dev environment)
  - Use reserved instances if keeping long-term
  - Right-size instance types
- [ ] ECS:
  - Review CPU/memory allocation
  - Consider spot instances (not available for Fargate)
  - Scale down when not in use
- [ ] Lambda:
  - Already very cost-effective
  - Review execution time optimization
- [ ] Data Transfer:
  - Minimize unnecessary data transfer
  - Use CloudFront for static content (if applicable)

#### 11.3 Cost Monitoring
- [ ] Set up billing alarms
- [ ] Configure budget alerts
- [ ] Document monthly cost estimate

#### 11.4 Cleanup Script (for testing)
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

### Lambda & SNS
- [ ] SNS topic created
- [ ] Lambda function deployed
- [ ] Email notifications working
- [ ] Events published from Spring Boot
- [ ] End-to-end flow tested

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

### Issue: Lambda Not Triggering
**Solution:** Check SNS subscription, Lambda permissions, CloudWatch logs

### Issue: Email Not Sending
**Solution:** Verify SES email address, check Lambda logs, verify SES is out of sandbox

### Issue: High Costs
**Solution:** Stop RDS when not in use, reduce ECS desired count, use smaller instance types

---

## 📈 Next Steps (After Completion)

### Optional Enhancements:
1. **Auto-scaling:**
   - Configure ECS auto-scaling based on CPU/memory
   - Implement ALB target scaling

2. **CI/CD Pipeline:**
   - Set up AWS CodePipeline
   - Automated testing and deployment

3. **Additional Lambda Functions:**
   - Scheduled task cleanup (EventBridge + Lambda)
   - Analytics aggregation (Lambda + DynamoDB)

4. **API Gateway:**
   - Migrate from ALB to API Gateway
   - Add rate limiting, caching

5. **Advanced Monitoring:**
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
- [ ] Lambda + SNS integrated
- [ ] Email notifications working
- [ ] Testing complete

### Week 4 Progress
- [ ] Monitoring configured
- [ ] Documentation complete
- [ ] Portfolio ready

---

## 🎯 Final Checklist Before Marking Complete

- [ ] All infrastructure deployed
- [ ] Both services running on ECS
- [ ] RDS migration complete
- [ ] Lambda + SNS working
- [ ] Email notifications tested
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
- [Lambda Best Practices](https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html)
- [SNS Documentation](https://docs.aws.amazon.com/sns/latest/dg/welcome.html)

### Tools
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS CLI Reference](https://docs.aws.amazon.com/cli/latest/reference/)
- [CloudWatch Dashboards](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Dashboards.html)

---

**Good luck with your deployment! 🚀**

