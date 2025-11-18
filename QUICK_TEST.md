# Quick Testing Guide

**Both services are now running!** ✅

## 🎉 Services Status

- ✅ **User Service:** `http://localhost:8081` (Running)
- ✅ **Task Service:** `http://localhost:8082` (Running)
- ✅ **PostgreSQL:** Connected and working

---

## 🧪 Quick Test Steps

### 1. Open Swagger UI (Easiest Way)

**User Service Swagger:**
```
http://localhost:8081/swagger-ui/index.html
```

**Task Service Swagger:**
```
http://localhost:8082/swagger-ui/index.html
```

### 2. Test Authentication Flow

#### Step 1: Register a User
- **Endpoint:** `POST /api/users`
- **Swagger:** Click on `/api/users` → POST → Try it out
- **Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```
- **Expected:** 200 OK with user details

#### Step 2: Login
- **Endpoint:** `POST /api/auth/login`
- **Swagger:** Click on `/api/auth/login` → POST → Try it out
- **Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```
- **Expected:** 200 OK with JWT token
- **⚠️ Important:** Copy the `token` from response!

#### Step 3: Authorize in Swagger
- Click the **"Authorize"** button (top right)
- Enter: `Bearer <paste-your-token-here>`
- Click "Authorize"
- Now you can test protected endpoints

### 3. Test Task Operations

#### Create Task
- **Endpoint:** `POST /api/tasks`
- **Swagger:** Click on `/api/tasks` → POST → Try it out
- **Body:**
```json
{
  "title": "My First Task",
  "description": "This is a test task",
  "status": "PENDING",
  "userId": 1
}
```
- **Expected:** 200 OK with created task
- **Note:** `userId` should match your logged-in user ID

#### Get All Tasks
- **Endpoint:** `GET /api/tasks`
- **Swagger:** Click on `/api/tasks` → GET → Try it out
- **Expected:** 200 OK with array of tasks (only your tasks)

#### Get Task by ID
- **Endpoint:** `GET /api/tasks/{id}`
- Replace `{id}` with a task ID from previous response
- **Expected:** 200 OK with task details

#### Update Task
- **Endpoint:** `PUT /api/tasks/{id}`
- **Body:**
```json
{
  "title": "Updated Task",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "userId": 1
}
```
- **Expected:** 200 OK with updated task

#### Delete Task
- **Endpoint:** `DELETE /api/tasks/{id}`
- **Expected:** 204 No Content

---

## 🔒 Test Authorization

### Test: User Cannot Access Other User's Task

1. **Register Second User:**
   - Register with username: `testuser2`

2. **Login as User 2:**
   - Get token for user 2

3. **Create Task for User 2:**
   - Note the task ID

4. **Login as User 1:**
   - Update token in Swagger

5. **Try to Access User 2's Task:**
   - `GET /api/tasks/{user2-task-id}`
   - **Expected:** 403 Forbidden

---

## ✅ Test Checklist

### Authentication
- [ ] Register user works
- [ ] Login returns JWT token
- [ ] Protected endpoints require token (401 without token)
- [ ] Invalid token is rejected (401)

### Task Operations
- [ ] Create task works
- [ ] Get all tasks returns only my tasks
- [ ] Get task by ID works for my tasks
- [ ] Update task works for my tasks
- [ ] Delete task works for my tasks

### Authorization
- [ ] Cannot access other user's task (403)
- [ ] Cannot update other user's task (403)
- [ ] Cannot delete other user's task (403)

### Swagger UI
- [ ] Both Swagger UIs accessible
- [ ] Authorization works with JWT token
- [ ] All endpoints documented

---

## 🐛 Troubleshooting

### If services are not responding:
```bash
# Check if they're running
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# If not running, start them:
cd backend/user-service && ./mvnw spring-boot:run
cd backend/task-service && ./mvnw spring-boot:run
```

### If getting 401 Unauthorized:
- Make sure you copied the full JWT token
- Format: `Bearer <token>` (include "Bearer " prefix)
- Token expires after 1 hour - login again if expired

### If getting 403 Forbidden:
- This is expected when trying to access other user's data
- Verify you're using the correct user's token

---

## 📊 Expected Results

| Test | Expected Result | Status |
|------|----------------|--------|
| Register User | 200 OK | ⬅️ Test it |
| Login | 200 OK with token | ⬅️ Test it |
| Create Task | 200 OK | ⬅️ Test it |
| Get Tasks | 200 OK (only my tasks) | ⬅️ Test it |
| Access Other User's Task | 403 Forbidden | ⬅️ Test it |

---

**Ready to test! Open Swagger UI and start testing! 🚀**

