# Testing Guide - Task Service JWT Protection

**Purpose:** Verify that task-service JWT authentication and authorization are working correctly.

---

## 📋 Prerequisites

### 1. Start Backend Services

**Ensure PostgreSQL is running and databases are created:**
```bash
# Connect to PostgreSQL and create databases if needed
psql postgres
CREATE DATABASE userdb;
CREATE DATABASE taskdb;
\q
```

**Start User Service (Terminal 1):**
```bash
cd backend/user-service
./mvnw spring-boot:run
```

**Start Task Service (Terminal 2):**
```bash
cd backend/task-service
./mvnw spring-boot:run
```

**Verify services are running:**
- User Service: `http://localhost:8081`
- Task Service: `http://localhost:8082`
- PostgreSQL databases: `userdb` and `taskdb` (running locally)

### 2. Test Tools

You can use:
- **Swagger UI** (recommended for interactive testing)
- **Postman** (for API collection)
- **cURL** (command line)
- **Browser** (for Swagger UI)

---

## 🧪 Testing Flow Overview

```
1. Register User 1
   ↓
2. Login User 1 → Get JWT Token 1
   ↓
3. Register User 2
   ↓
4. Login User 2 → Get JWT Token 2
   ↓
5. Test Task Operations with User 1 Token
   ↓
6. Test Authorization (User 1 cannot access User 2's tasks)
   ↓
7. Test Unauthorized Access (requests without token)
```

---

## ✅ Test Cases

### **Test 1: User Registration**

**Endpoint:** `POST http://localhost:8081/api/users`

**Request:**
```json
{
  "username": "testuser1",
  "email": "testuser1@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User1"
}
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "username": "testuser1",
  "email": "testuser1@example.com",
  "firstName": "Test",
  "lastName": "User1",
  "createdAt": "2025-01-16T...",
  "updatedAt": "2025-01-16T..."
}
```

**✅ Pass Criteria:**
- Status code: 200
- User ID is returned
- All fields match request

---

### **Test 2: User Login (Get JWT Token)**

**Endpoint:** `POST http://localhost:8081/api/auth/login`

**Request:**
```json
{
  "username": "testuser1",
  "password": "password123"
}
```

**Expected Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "testuser1"
}
```

**✅ Pass Criteria:**
- Status code: 200
- Token is returned (long JWT string)
- userId and username match

**⚠️ Important:** Save this token! You'll need it for all task operations.

**Example:**
```bash
# Save token to variable (bash/zsh)
TOKEN1="eyJhbGciOiJIUzI1NiJ9..."
USER_ID1=1
```

---

### **Test 3: Create Task (With JWT Token)**

**Endpoint:** `POST http://localhost:8082/api/tasks`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request:**
```json
{
  "title": "Test Task 1",
  "description": "This is a test task",
  "status": "PENDING",
  "userId": 1
}
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Test Task 1",
  "description": "This is a test task",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-01-16T...",
  "updatedAt": "2025-01-16T..."
}
```

**✅ Pass Criteria:**
- Status code: 200
- Task is created with correct userId
- All fields match request

**⚠️ Note:** Even if you send a different `userId` in the request, it should be overridden with the authenticated user's ID.

**Test with wrong userId:**
```json
{
  "title": "Test Task",
  "description": "Test",
  "userId": 999  // Wrong userId
}
```

**Expected:** Task is still created with authenticated user's ID (1), not 999.

---

### **Test 4: Get All Tasks (With JWT Token)**

**Endpoint:** `GET http://localhost:8082/api/tasks`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Test Task 1",
    "description": "This is a test task",
    "status": "PENDING",
    "userId": 1,
    "createdAt": "2025-01-16T...",
    "updatedAt": "2025-01-16T..."
  }
]
```

**✅ Pass Criteria:**
- Status code: 200
- Only returns tasks for authenticated user (userId: 1)
- Does NOT return tasks from other users

---

### **Test 5: Get Task by ID (With JWT Token)**

**Endpoint:** `GET http://localhost:8082/api/tasks/1`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Test Task 1",
  "description": "This is a test task",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-01-16T...",
  "updatedAt": "2025-01-16T..."
}
```

**✅ Pass Criteria:**
- Status code: 200
- Returns task if it belongs to authenticated user

---

### **Test 6: Update Task (With JWT Token)**

**Endpoint:** `PUT http://localhost:8082/api/tasks/1`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request:**
```json
{
  "title": "Updated Task Title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "userId": 1
}
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Updated Task Title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "userId": 1,
  "createdAt": "2025-01-16T...",
  "updatedAt": "2025-01-16T..."  // Updated timestamp
}
```

**✅ Pass Criteria:**
- Status code: 200
- Task is updated successfully
- Updated timestamp changes

---

### **Test 7: Delete Task (With JWT Token)**

**Endpoint:** `DELETE http://localhost:8082/api/tasks/1`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Expected Response:** `204 No Content`

**✅ Pass Criteria:**
- Status code: 204
- Task is deleted
- Verify by calling `GET /api/tasks/1` → Should return 404

---

## 🔒 Authorization Tests (Critical!)

### **Test 8: Create Second User and Tasks**

**Step 1: Register User 2**
```json
POST http://localhost:8081/api/users
{
  "username": "testuser2",
  "email": "testuser2@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User2"
}
```

**Step 2: Login User 2**
```json
POST http://localhost:8081/api/auth/login
{
  "username": "testuser2",
  "password": "password123"
}
```

**Save Token 2:**
```bash
TOKEN2="<token-from-response>"
USER_ID2=2
```

**Step 3: Create Task for User 2**
```json
POST http://localhost:8082/api/tasks
Authorization: Bearer <TOKEN2>

{
  "title": "User 2 Task",
  "description": "This belongs to user 2",
  "status": "PENDING",
  "userId": 2
}
```

**Expected:** Task created with id: 2, userId: 2

---

### **Test 9: User 1 Cannot Access User 2's Task (Authorization Check)**

**Endpoint:** `GET http://localhost:8082/api/tasks/2`

**Headers:**
```
Authorization: Bearer <TOKEN1>  // User 1's token
```

**Expected Response:** `403 Forbidden`
```json
{
  "error": "Forbidden: Cannot access other user's tasks"
}
```

**✅ Pass Criteria:**
- Status code: 403
- Error message indicates authorization failure
- User 1 cannot see User 2's task

---

### **Test 10: User 1 Cannot Update User 2's Task**

**Endpoint:** `PUT http://localhost:8082/api/tasks/2`

**Headers:**
```
Authorization: Bearer <TOKEN1>  // User 1's token
Content-Type: application/json
```

**Request:**
```json
{
  "title": "Hacked Task",
  "description": "Trying to modify user 2's task",
  "status": "COMPLETED",
  "userId": 2
}
```

**Expected Response:** `403 Forbidden`
```json
{
  "error": "Forbidden: Cannot modify other user's tasks"
}
```

**✅ Pass Criteria:**
- Status code: 403
- Task is NOT modified
- Error message indicates authorization failure

---

### **Test 11: User 1 Cannot Delete User 2's Task**

**Endpoint:** `DELETE http://localhost:8082/api/tasks/2`

**Headers:**
```
Authorization: Bearer <TOKEN1>  // User 1's token
```

**Expected Response:** `403 Forbidden`
```json
{
  "error": "Forbidden: Cannot delete other user's tasks"
}
```

**✅ Pass Criteria:**
- Status code: 403
- Task is NOT deleted
- Error message indicates authorization failure

**Verify:** User 2 can still access their task:
```bash
GET http://localhost:8082/api/tasks/2
Authorization: Bearer <TOKEN2>
```
Should return 200 OK with the task.

---

## 🚫 Unauthorized Access Tests

### **Test 12: Access Task Endpoint Without Token**

**Endpoint:** `GET http://localhost:8082/api/tasks`

**Headers:** (No Authorization header)

**Expected Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized: Missing or invalid token"
}
```

**✅ Pass Criteria:**
- Status code: 401
- Error message indicates missing token

---

### **Test 13: Access Task Endpoint With Invalid Token**

**Endpoint:** `GET http://localhost:8082/api/tasks`

**Headers:**
```
Authorization: Bearer invalid-token-here
```

**Expected Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized: Invalid or expired token"
}
```

**✅ Pass Criteria:**
- Status code: 401
- Error message indicates invalid token

---

### **Test 14: Access Task Endpoint With Expired Token**

**Note:** Tokens expire after 1 hour. If you have an old token, test with it.

**Expected Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized: Invalid or expired token"
}
```

**✅ Pass Criteria:**
- Status code: 401
- Expired tokens are rejected

---

## 📊 Swagger UI Testing

### **Access Swagger UI**

1. **User Service Swagger:**
   - URL: `http://localhost:8081/swagger-ui/index.html`
   - Test user endpoints here

2. **Task Service Swagger:**
   - URL: `http://localhost:8082/swagger-ui/index.html`
   - Test task endpoints here

### **Using Swagger UI:**

1. Click "Authorize" button (top right)
2. Enter: `Bearer <your-jwt-token>`
3. Click "Authorize"
4. Now you can test all protected endpoints
5. Click on any endpoint → "Try it out" → Enter data → "Execute"

---

## 🧪 Complete Test Script (cURL)

Save this as `test-api.sh`:

```bash
#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_USER_URL="http://localhost:8081"
BASE_TASK_URL="http://localhost:8082"

echo "=== Testing Todo Application APIs ==="

# 1. Register User 1
echo -e "\n${GREEN}1. Registering User 1...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_USER_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "testuser1@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User1"
  }')
echo "$REGISTER_RESPONSE"

# Extract user ID
USER_ID1=$(echo $REGISTER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "User ID: $USER_ID1"

# 2. Login User 1
echo -e "\n${GREEN}2. Logging in User 1...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_USER_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "password123"
  }')
echo "$LOGIN_RESPONSE"

# Extract token
TOKEN1=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: ${TOKEN1:0:50}..."

# 3. Create Task
echo -e "\n${GREEN}3. Creating Task...${NC}"
CREATE_TASK_RESPONSE=$(curl -s -X POST "$BASE_TASK_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN1" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Test Task\",
    \"description\": \"This is a test task\",
    \"status\": \"PENDING\",
    \"userId\": $USER_ID1
  }")
echo "$CREATE_TASK_RESPONSE"

# Extract task ID
TASK_ID=$(echo $CREATE_TASK_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Task ID: $TASK_ID"

# 4. Get All Tasks
echo -e "\n${GREEN}4. Getting All Tasks...${NC}"
GET_TASKS_RESPONSE=$(curl -s -X GET "$BASE_TASK_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN1")
echo "$GET_TASKS_RESPONSE"

# 5. Get Task by ID
echo -e "\n${GREEN}5. Getting Task by ID...${NC}"
GET_TASK_RESPONSE=$(curl -s -X GET "$BASE_TASK_URL/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN1")
echo "$GET_TASK_RESPONSE"

# 6. Update Task
echo -e "\n${GREEN}6. Updating Task...${NC}"
UPDATE_TASK_RESPONSE=$(curl -s -X PUT "$BASE_TASK_URL/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN1" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Updated Task\",
    \"description\": \"Updated description\",
    \"status\": \"IN_PROGRESS\",
    \"userId\": $USER_ID1
  }")
echo "$UPDATE_TASK_RESPONSE"

# 7. Test Unauthorized Access (No Token)
echo -e "\n${RED}7. Testing Unauthorized Access (No Token)...${NC}"
UNAUTHORIZED_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X GET "$BASE_TASK_URL/api/tasks")
HTTP_CODE=$(echo "$UNAUTHORIZED_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
if [ "$HTTP_CODE" == "401" ]; then
  echo -e "${GREEN}✓ Unauthorized access correctly blocked${NC}"
else
  echo -e "${RED}✗ Expected 401, got $HTTP_CODE${NC}"
fi

# 8. Register User 2
echo -e "\n${GREEN}8. Registering User 2...${NC}"
REGISTER_USER2_RESPONSE=$(curl -s -X POST "$BASE_USER_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "testuser2@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User2"
  }')
echo "$REGISTER_USER2_RESPONSE"

# 9. Login User 2
echo -e "\n${GREEN}9. Logging in User 2...${NC}"
LOGIN_USER2_RESPONSE=$(curl -s -X POST "$BASE_USER_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "password": "password123"
  }')
echo "$LOGIN_USER2_RESPONSE"

TOKEN2=$(echo $LOGIN_USER2_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 10. Test Authorization (User 2 cannot access User 1's task)
echo -e "\n${RED}10. Testing Authorization (User 2 accessing User 1's task)...${NC}"
FORBIDDEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X GET "$BASE_TASK_URL/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN2")
HTTP_CODE=$(echo "$FORBIDDEN_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
if [ "$HTTP_CODE" == "403" ]; then
  echo -e "${GREEN}✓ Authorization correctly enforced${NC}"
else
  echo -e "${RED}✗ Expected 403, got $HTTP_CODE${NC}"
fi

echo -e "\n${GREEN}=== Testing Complete ===${NC}"
```

**Run the script:**
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## ✅ Test Checklist

### Authentication Tests
- [ ] User can register
- [ ] User can login and receive JWT token
- [ ] Task endpoints require JWT token (401 without token)
- [ ] Invalid token is rejected (401)
- [ ] Expired token is rejected (401)

### Authorization Tests
- [ ] User can only see their own tasks
- [ ] User cannot access other user's task (403)
- [ ] User cannot update other user's task (403)
- [ ] User cannot delete other user's task (403)
- [ ] Task creation automatically uses authenticated user's ID

### CRUD Operations Tests
- [ ] Create task works with valid token
- [ ] Get all tasks returns only user's tasks
- [ ] Get task by ID works for own tasks
- [ ] Update task works for own tasks
- [ ] Delete task works for own tasks

### Swagger UI Tests
- [ ] Swagger UI accessible at `/swagger-ui/index.html`
- [ ] Can authorize with JWT token
- [ ] Can test endpoints from Swagger UI

---

## 🐛 Troubleshooting

### Issue: 401 Unauthorized on all requests
**Solution:**
- Check token is included in `Authorization: Bearer <token>` header
- Verify token is not expired (tokens last 1 hour)
- Ensure JWT secret matches between user-service and task-service

### Issue: 403 Forbidden when accessing own tasks
**Solution:**
- Check that `userId` in task matches authenticated user's ID
- Verify `CurrentUser.getUserId()` is working correctly
- Check JWT token contains correct `userId` claim

### Issue: Can access other user's tasks
**Solution:**
- Verify authorization checks in `TaskController` are working
- Check that `CurrentUser.getUserId()` returns correct value
- Ensure task's `userId` is being compared correctly

### Issue: Build errors
**Solution:**
```bash
cd backend/task-service
./mvnw clean compile
```

### Issue: Services not starting
**Solution:**
```bash
# Check if ports are in use
lsof -i :8081
lsof -i :8082

# Check PostgreSQL is running
psql -h localhost -U sarptekin -d postgres

# Restart services
cd backend/user-service
./mvnw spring-boot:run

# In another terminal
cd backend/task-service
./mvnw spring-boot:run
```

---

## 📝 Expected Test Results Summary

| Test | Endpoint | Token | Expected Status | Pass/Fail |
|------|----------|-------|-----------------|-----------|
| Register User | POST /api/users | None | 200 | ✅ |
| Login | POST /api/auth/login | None | 200 | ✅ |
| Create Task | POST /api/tasks | Valid | 200 | ✅ |
| Get Tasks | GET /api/tasks | Valid | 200 | ✅ |
| Get Task | GET /api/tasks/{id} | Valid (own) | 200 | ✅ |
| Get Task | GET /api/tasks/{id} | Valid (other) | 403 | ✅ |
| Update Task | PUT /api/tasks/{id} | Valid (own) | 200 | ✅ |
| Update Task | PUT /api/tasks/{id} | Valid (other) | 403 | ✅ |
| Delete Task | DELETE /api/tasks/{id} | Valid (own) | 204 | ✅ |
| Delete Task | DELETE /api/tasks/{id} | Valid (other) | 403 | ✅ |
| Any Task Endpoint | Any | None | 401 | ✅ |
| Any Task Endpoint | Any | Invalid | 401 | ✅ |

---

## 🎯 Success Criteria

**All tests pass if:**
1. ✅ All endpoints require JWT authentication
2. ✅ Users can only access their own tasks
3. ✅ Authorization checks prevent unauthorized access
4. ✅ Error messages are clear and helpful
5. ✅ Swagger UI works and shows all endpoints

**If all tests pass → Task Service is production-ready! 🎉**

---

**Ready to test? Start with the Swagger UI or use the cURL script above!**

