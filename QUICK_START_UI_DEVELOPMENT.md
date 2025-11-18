# 🚀 Quick Start - UI Development

**Status:** ✅ Both backend services are running!

---

## 📡 Service URLs

### **For Local Testing (Browser/Postman):**
- **User Service:** `http://localhost:8081`
- **Task Service:** `http://localhost:8082`

### **For Android Emulator:**
- **User Service:** `http://10.0.2.2:8081`
- **Task Service:** `http://10.0.2.2:8082`

### **For Physical Android Device (Same Network):**
- **User Service:** `http://<your-computer-ip>:8081`
- **Task Service:** `http://<your-computer-ip>:8082`

**To find your IP:**
```bash
# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1

# Or
ipconfig getifaddr en0  # macOS
```

---

## 🔍 Swagger UI (API Testing)

**Test APIs directly in browser:**
- **User Service Swagger:** http://localhost:8081/swagger-ui.html
- **Task Service Swagger:** http://localhost:8082/swagger-ui.html

**Features:**
- ✅ Test all endpoints
- ✅ See request/response formats
- ✅ Authorize with JWT token
- ✅ Interactive API documentation

---

## 🧪 Quick Test

### **1. Register a User:**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### **2. Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Save the token from the response!**

### **3. Create a Task (with token):**
```bash
curl -X POST http://localhost:8082/api/tasks \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Task",
    "description": "This is a test task",
    "status": "PENDING"
  }'
```

---

## 📋 Service Status

**Check if services are running:**
```bash
# User Service
curl http://localhost:8081/actuator/health

# Task Service
curl http://localhost:8082/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

---

## 🛑 Stop Services

**To stop services:**
```bash
# Stop user-service
pkill -f "user-service"

# Stop task-service
pkill -f "task-service"

# Or stop both
pkill -f "user-service"; pkill -f "task-service"
```

---

## 🔄 Restart Services

**To restart services:**
```bash
# From project root
cd backend/user-service && ./mvnw spring-boot:run &
cd ../task-service && ./mvnw spring-boot:run &
```

---

## 📝 Logs

**View service logs:**
```bash
# User Service logs
tail -f /tmp/user-service.log

# Task Service logs
tail -f /tmp/task-service.log
```

---

## ✅ Verification Checklist

Before starting UI development, verify:

- [ ] User Service is running (http://localhost:8081/actuator/health returns `{"status":"UP"}`)
- [ ] Task Service is running (http://localhost:8082/actuator/health returns `{"status":"UP"}`)
- [ ] Swagger UI accessible (http://localhost:8081/swagger-ui.html)
- [ ] Can register a user
- [ ] Can login and get JWT token
- [ ] Can create a task with JWT token

---

## 🎯 Next Steps for UI Development

1. **Set up Kotlin Android project**
2. **Add Retrofit for API calls**
3. **Create data models** (User, Task, LoginRequest, etc.)
4. **Implement authentication flow**
5. **Build task management screens**
6. **Test with running backend services**

**Full documentation:** See `KOTLIN_UI_DEVELOPMENT_PROMPT.md`

---

## 💡 Tips

- **Use Swagger UI** to test APIs before implementing in Kotlin
- **Save JWT tokens** securely (EncryptedSharedPreferences)
- **Handle 401 errors** (token expired) by redirecting to login
- **Test with Postman** first to understand API behavior
- **Check logs** if something doesn't work

---

**Happy coding! 🚀**

