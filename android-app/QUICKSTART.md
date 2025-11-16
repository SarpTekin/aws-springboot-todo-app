# Quick Start Guide

Get the MicroTodo Android app running in 5 minutes!

## Prerequisites Checklist

- [ ] Android Studio Hedgehog (2023.1.1) or later
- [ ] JDK 17 or later
- [ ] Backend services running on ports 8081 and 8082

## Step 1: Start Backend Services (2 minutes)

Open two terminal windows:

**Terminal 1 - User Service:**
```bash
cd backend/user-service
./gradlew bootRun
```

**Terminal 2 - Task Service:**
```bash
cd backend/task-service
./gradlew bootRun
```

**Verify:** Open browser to http://localhost:8081/api/users and http://localhost:8082/api/tasks

## Step 2: Open Android Project (1 minute)

```bash
cd android-app
# Open this folder in Android Studio
```

Or in Android Studio:
1. File → Open
2. Navigate to `android-app` folder
3. Click "Open"

## Step 3: Sync Gradle (1 minute)

Android Studio will automatically prompt to sync. Click "Sync Now"

Or manually:
- File → Sync Project with Gradle Files

Wait for sync to complete (~30 seconds)

## Step 4: Run the App (1 minute)

**Option A: Using Emulator**
1. Click the device dropdown (top toolbar)
2. Select "Create New Virtual Device" if no emulator exists
3. Choose "Pixel 5" → Next → Download "Tiramisu" (API 33) → Finish
4. Click ▶️ Run button

**Option B: Physical Device**
1. Enable Developer Options on your Android phone
2. Enable USB Debugging
3. Connect via USB
4. Select your device from dropdown
5. Click ▶️ Run button

**Important for Physical Device:**
Edit `app/build.gradle.kts` and change:
```kotlin
// Find your computer's IP: ipconfig (Windows) or ifconfig (Mac/Linux)
buildConfigField("String", "USER_SERVICE_BASE_URL", "\"http://YOUR_IP:8081\"")
buildConfigField("String", "TASK_SERVICE_BASE_URL", "\"http://YOUR_IP:8082\"")
```

## Step 5: Test the App

### Create Account
1. App launches to Login screen
2. Tap "Don't have an account? Sign Up"
3. Fill in:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `password123`
4. Tap "Sign Up"

### Login
1. Back on Login screen
2. Enter:
   - Username: `testuser`
   - Password: `password123`
3. Tap "Sign In"

### Create Tasks
1. You're now on Tasks screen
2. Tap the **+** floating button
3. Enter task title: "My First Task"
4. Tap "Create"

### Manage Tasks
- Tap **⋮** menu on task
- Select "Mark In Progress" or "Mark Completed"
- Or select "Delete" to remove

## Common Issues

### Issue: "Failed to connect to /10.0.2.2:8081"

**Check:** Are backend services running?
```bash
curl http://localhost:8081/api/users
curl http://localhost:8082/api/tasks
```

### Issue: "Cannot resolve symbol 'BuildConfig'"

**Fix:** Clean and rebuild
```bash
./gradlew clean build
```

### Issue: App crashes on launch

**Check Logcat:**
```bash
adb logcat | grep -i error
```

Look for stack traces and error messages

### Issue: "401 Unauthorized"

**Cause:** JWT token expired (1 hour lifetime)

**Fix:** Logout and login again

## Verify Everything Works

### Backend Health
```bash
# User Service
curl http://localhost:8081/api/users

# Task Service
curl http://localhost:8082/api/tasks
```

### Database Connection
Check your PostgreSQL databases:
```bash
psql -U sarptekin -d userdb -c "SELECT * FROM users;"
psql -U sarptekin -d taskdb -c "SELECT * FROM tasks;"
```

### Android App Logs
```bash
adb logcat | grep "MicroTodo"
```

## Testing the App

Run unit tests:
```bash
./gradlew test
```

Expected output:
```
> Task :app:testDebugUnitTest
LoginViewModelTest > initial state should be empty PASSED
LoginViewModelTest > login success should update state correctly PASSED
TasksViewModelTest > createTask success should add task to list PASSED
...

BUILD SUCCESSFUL in 12s
```

## Next Steps

1. **Read Architecture**: Check `ARCHITECTURE.md` for detailed architecture explanation
2. **Review Code**: Browse through the clean architecture layers
3. **Run Tests**: `./gradlew test --info` for detailed test output
4. **Customize**: Modify UI theme in `ui/theme/Theme.kt`
5. **Deploy**: See "Production Checklist" in README.md

## Development Workflow

### Making Changes

1. Edit code in Android Studio
2. Hot reload: Ctrl+F9 (Win) or Cmd+F9 (Mac)
3. Full rebuild: Clean → Build → Run

### Debugging

1. Set breakpoints in code
2. Click Debug button (🐛)
3. Step through code

### Viewing API Calls

Check Logcat for HTTP requests:
```bash
adb logcat | grep "OkHttp"
```

You'll see:
```
D/OkHttp: --> POST http://10.0.2.2:8081/api/auth/login
D/OkHttp: {"username":"testuser","password":"password123"}
D/OkHttp: <-- 200 OK (145ms)
```

## Tips for Success

1. **Keep Backend Running**: Don't stop backend services while testing
2. **Clear App Data**: Settings → Apps → MicroTodo → Clear Data (resets token)
3. **Check Network**: Ensure emulator/device has internet
4. **Use Logcat**: Your best friend for debugging
5. **Test on Real Device**: Emulators can be slow

## Troubleshooting Commands

```bash
# Restart ADB
adb kill-server && adb start-server

# Clear app data
adb shell pm clear com.microtodo.android

# Uninstall app
adb uninstall com.microtodo.android

# Reinstall
./gradlew installDebug

# View app logs
adb logcat -c && adb logcat | grep MicroTodo
```

## Success Checklist

After following this guide, you should have:

- [x] Backend services running on 8081 and 8082
- [x] Android app installed on emulator/device
- [x] Successfully registered a user
- [x] Logged in with JWT authentication
- [x] Created, updated, and deleted tasks
- [x] Verified network calls in Logcat

## Need Help?

1. Check `README.md` for detailed troubleshooting
2. Review `ARCHITECTURE.md` for understanding the code
3. Inspect Logcat: `adb logcat | grep -E "(MicroTodo|OkHttp|ERROR)"`
4. Check backend logs for API errors

---

**You're all set!** Start exploring the code and building features. 🚀
