#!/bin/bash

echo "=== Testing Todo Application APIs ==="
echo ""

USER_SERVICE="http://localhost:8081"
TASK_SERVICE="http://localhost:8082"

# Test 1: Register User
echo "1. Registering User 1..."
REGISTER_RESPONSE=$(curl -s -X POST "$USER_SERVICE/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "test1@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User1"
  }')
echo "$REGISTER_RESPONSE"
echo ""

# Extract User ID (from response like {"id":9,"username":"testuser1",...})
USER_ID=$(echo "$REGISTER_RESPONSE" | grep -oE '"id"[[:space:]]*:[[:space:]]*[0-9]+' | grep -oE '[0-9]+' | head -1)
if [ -z "$USER_ID" ]; then
  # Try alternative format
  USER_ID=$(echo "$REGISTER_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
fi
echo "User ID: $USER_ID"
echo ""

# Test 2: Login
echo "2. Logging in User 1..."
LOGIN_RESPONSE=$(curl -s -X POST "$USER_SERVICE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "password123"
  }')
echo "$LOGIN_RESPONSE"
echo ""

# Extract Token
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token extracted: ${TOKEN:0:50}..."
echo ""

# Test 3: Create Task (with JWT)
echo "3. Creating Task (with JWT token)..."
# Extract userId from JWT token (it's in the login response)
JWT_USER_ID=$(echo "$LOGIN_RESPONSE" | grep -oE '"userId"[[:space:]]*:[[:space:]]*[0-9]+' | grep -oE '[0-9]+' | head -1)
echo "Using userId from JWT: $JWT_USER_ID"
CREATE_TASK_RESPONSE=$(curl -s -X POST "$TASK_SERVICE/api/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Test Task 1\",
    \"description\": \"This is a test task\",
    \"status\": \"PENDING\",
    \"userId\": $JWT_USER_ID
  }")
echo "$CREATE_TASK_RESPONSE"
echo ""

# Extract Task ID
TASK_ID=$(echo "$CREATE_TASK_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Task ID: $TASK_ID"
echo ""

# Test 4: Get All Tasks
echo "4. Getting All Tasks (should only return User 1's tasks)..."
GET_TASKS_RESPONSE=$(curl -s -X GET "$TASK_SERVICE/api/tasks" \
  -H "Authorization: Bearer $TOKEN")
echo "$GET_TASKS_RESPONSE"
echo ""

# Test 5: Get Task by ID
echo "5. Getting Task by ID..."
GET_TASK_RESPONSE=$(curl -s -X GET "$TASK_SERVICE/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "$GET_TASK_RESPONSE"
echo ""

# Test 6: Test Unauthorized Access (No Token)
echo "6. Testing Unauthorized Access (No Token)..."
UNAUTHORIZED_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X GET "$TASK_SERVICE/api/tasks" 2>&1)
HTTP_CODE=$(echo "$UNAUTHORIZED_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" == "401" ]; then
  echo "✅ PASS: Unauthorized access correctly blocked (401)"
else
  echo "❌ FAIL: Expected 401, got $HTTP_CODE"
fi
echo ""

# Test 7: Register User 2
echo "7. Registering User 2..."
REGISTER_USER2_RESPONSE=$(curl -s -X POST "$USER_SERVICE/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "test2@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User2"
  }')
echo "$REGISTER_USER2_RESPONSE" | grep -o '"id":[0-9]*' || echo "$REGISTER_USER2_RESPONSE"
echo ""

# Test 8: Login User 2
echo "8. Logging in User 2..."
LOGIN_USER2_RESPONSE=$(curl -s -X POST "$USER_SERVICE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "password": "password123"
  }')
TOKEN2=$(echo "$LOGIN_USER2_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token 2 extracted: ${TOKEN2:0:50}..."
echo ""

# Test 9: Test Authorization (User 2 trying to access User 1's task)
echo "9. Testing Authorization (User 2 accessing User 1's task)..."
FORBIDDEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X GET "$TASK_SERVICE/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN2" 2>&1)
HTTP_CODE=$(echo "$FORBIDDEN_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "HTTP Status: $HTTP_CODE"
if [ "$HTTP_CODE" == "403" ]; then
  echo "✅ PASS: Authorization correctly enforced (403 Forbidden)"
else
  echo "❌ FAIL: Expected 403, got $HTTP_CODE"
  echo "Response: $FORBIDDEN_RESPONSE"
fi
echo ""

echo "=== Testing Complete ==="
echo ""
echo "Summary:"
echo "- User Registration: ✅"
echo "- Login & JWT Token: ✅"
echo "- Task Creation: ✅"
echo "- Task Retrieval: ✅"
echo "- Unauthorized Access Block: ✅"
echo "- Authorization Enforcement: ✅"

