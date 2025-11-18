# AWS Architecture - Simplified (No Docker/Lambda)

## 🏗️ Target Architecture

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

## 📦 Core AWS Services

### 1. **AWS Elastic Beanstalk** - Application Deployment
- **What:** Deploys Spring Boot JAR files directly (no Docker needed)
- **Why:** Managed platform, auto-scaling, easy deployment
- **Cost:** ~$15-20/month for 2 environments (EC2 instances)
- **Portfolio Value:** ⭐⭐⭐ High

### 2. **Amazon RDS PostgreSQL** - Database
- **What:** Managed PostgreSQL database
- **Why:** Automated backups, high availability, monitoring
- **Cost:** ~$15-20/month per instance (db.t3.micro)
- **Portfolio Value:** ⭐⭐⭐ High

### 3. **Application Load Balancer (ALB)** - Traffic Routing
- **What:** Routes traffic to services (auto-created by Elastic Beanstalk)
- **Why:** Health checks, SSL termination, high availability
- **Cost:** ~$20-25/month per ALB (2 ALBs = ~$40-50)
- **Portfolio Value:** ⭐⭐⭐ High

### 4. **AWS Secrets Manager** - Secrets Management
- **What:** Secure storage for credentials
- **Why:** Encrypted secrets, rotation support
- **Cost:** ~$0.40 per secret/month
- **Portfolio Value:** ⭐⭐ Medium

### 5. **Amazon CloudWatch** - Monitoring
- **What:** Logs, metrics, alarms
- **Why:** Application monitoring, debugging
- **Cost:** ~$5-10/month
- **Portfolio Value:** ⭐⭐ Medium

### 6. **AWS Certificate Manager (ACM)** - SSL Certificates
- **What:** Free SSL/TLS certificates
- **Why:** HTTPS encryption
- **Cost:** FREE
- **Portfolio Value:** ⭐ Medium

---

## 🎯 AWS Technologies for Your Portfolio

### **High Portfolio Value:**
1. ✅ **Elastic Beanstalk** - Managed application deployment (in-demand skill)
2. ✅ **RDS PostgreSQL** - Managed databases (essential skill)
3. ✅ **ALB** - Load balancing (infrastructure knowledge)

### **Medium Portfolio Value:**
4. ✅ **Secrets Manager** - Security best practices
5. ✅ **CloudWatch** - Monitoring and observability
6. ✅ **VPC & Security Groups** - Network security

### **Bonus:**
7. ✅ **Terraform** - Infrastructure as Code (highly valued)
8. ✅ **CI/CD Pipeline** - Automation (if time permits)

---

## 💰 Cost Estimation

### Monthly Costs (USD):

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
- Use single ALB (requires custom setup): Save ~$20-25/month

**Optimized Total:** ~$5-25/month (when services stopped)

---

## 📈 Portfolio Value Summary

### **What This Shows:**
- ✅ Microservices architecture on AWS
- ✅ Managed application deployment with Elastic Beanstalk
- ✅ Managed database administration (RDS)
- ✅ Load balancing and high availability
- ✅ Infrastructure as Code (Terraform - optional)
- ✅ Security best practices (Secrets Manager)
- ✅ Monitoring and observability (CloudWatch)
- ✅ Production-ready deployment

### **CV Bullet Points:**
- "Deployed Spring Boot microservices on AWS Elastic Beanstalk"
- "Migrated PostgreSQL to Amazon RDS with automated backups"
- "Configured Application Load Balancer for high availability"
- "Integrated AWS Secrets Manager for secure credential management"
- "Set up comprehensive monitoring with CloudWatch"
- "Implemented Infrastructure as Code using Terraform" (optional)

---

## 🚀 Deployment Phases

### **Phase 1: Core Infrastructure (Week 1)**
- Build Spring Boot JARs
- RDS instances setup
- Elastic Beanstalk environments
- Secrets Manager
- Database migration

### **Phase 2: Application Deployment (Week 2)**
- Deploy JARs to Elastic Beanstalk
- Configure environment variables
- Test endpoints
- CloudWatch monitoring
- SSL certificates (HTTPS)
- Security hardening
- Documentation

---

**This simplified architecture still demonstrates excellent AWS skills while being easier to implement and maintain!**

