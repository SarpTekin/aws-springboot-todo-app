# Git Workflow for AI Collaboration

This guide explains the Git branching strategy for coordinating between Claude Code (frontend) and Cursor AI (backend).

## 🌳 Branch Strategy

```
main (production-ready code)
  ├── feature/android-app              ← Claude Code's work
  ├── feature/android-task-filters     ← Future Claude features
  ├── backend/add-pagination           ← Cursor AI's work
  ├── backend/jwt-refresh              ← Cursor AI's work
  └── docs/api-updates                 ← Documentation
```

## 📋 Branch Naming Convention

Use **prefixes** to indicate the type of work:

| Prefix | Purpose | Example | Who |
|--------|---------|---------|-----|
| `feature/` | New frontend features | `feature/android-offline-mode` | Claude Code |
| `backend/` | Backend improvements | `backend/add-websockets` | Cursor AI |
| `fix/` | Bug fixes | `fix/login-crash` | Either AI |
| `refactor/` | Code improvements | `refactor/clean-architecture` | Either AI |
| `docs/` | Documentation only | `docs/api-swagger` | Either AI |
| `test/` | Testing additions | `test/integration-tests` | Either AI |
| `hotfix/` | Urgent production fixes | `hotfix/security-patch` | Either AI |

## 🔄 Complete Workflow

### **Step 1: Start New Work**

**For Claude Code (Frontend):**
```bash
# Always start from latest main
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/android-task-search

# Do work...
# Commit changes
git add .
git commit -m "feat(android): Add task search functionality"

# Push to GitHub
git push -u origin feature/android-task-search
```

**For Cursor AI (Backend):**
```bash
# Start from main
git checkout main
git pull origin main

# Create backend branch
git checkout -b backend/add-task-priority

# Do work...
# Commit changes
git add backend/
git commit -m "feat(backend): Add priority field to Task entity"

# Push to GitHub
git push -u origin backend/add-task-priority
```

### **Step 2: Create Pull Request (PR)**

On GitHub:
1. Go to: https://github.com/SarpTekin/aws-springboot-todo-app/pulls
2. Click "New Pull Request"
3. Select your branch (e.g., `feature/android-app`)
4. Base branch: `main`
5. Add description of changes
6. Create PR (don't merge yet)

### **Step 3: Review and Test**

**Before merging ANY branch:**

```bash
# Test frontend with backend changes
git checkout main
git pull origin main

# Test backend changes with frontend
git checkout feature/android-app
git pull origin backend/add-task-priority

# Run tests
cd android-app && ./gradlew test
cd backend/user-service && ./gradlew test
cd backend/task-service && ./gradlew test
```

### **Step 4: Merge to Main**

**Option A: Merge via GitHub (Recommended)**
1. Go to Pull Request on GitHub
2. Click "Merge Pull Request"
3. Choose "Squash and Merge" (cleaner history)
4. Delete branch after merge

**Option B: Merge locally**
```bash
git checkout main
git pull origin main
git merge --no-ff feature/android-app
git push origin main
git branch -d feature/android-app  # Delete local branch
git push origin --delete feature/android-app  # Delete remote branch
```

## 🤝 Coordination Between AIs

### **Scenario 1: Backend API Change Affects Frontend**

**Cursor AI** adds a new field to Task entity:

```bash
# Cursor AI's work
git checkout -b backend/add-task-priority
# Adds 'priority' field to Task.java
git commit -m "feat(backend): Add priority field to Task"
git push -u origin backend/add-task-priority
```

**You inform Claude Code:**
> "Cursor added a `priority` field (enum: LOW, MEDIUM, HIGH) to Task. Update Android app."

**Claude Code** updates frontend:
```bash
git checkout -b feature/android-task-priority
# Updates TaskDtos.kt to include priority field
# Updates UI to show priority
git commit -m "feat(android): Add task priority support"
git push -u origin feature/android-task-priority
```

**Merge order:**
1. Merge `backend/add-task-priority` first (backend must be deployed)
2. Then merge `feature/android-task-priority` (frontend uses new API)

### **Scenario 2: Both AIs Working Simultaneously**

**Claude Code** working on:
- `feature/android-dark-mode` (UI only, no backend changes)

**Cursor AI** working on:
- `backend/improve-performance` (Backend optimization)

**Safe to merge independently** because they don't affect each other.

### **Scenario 3: Conflicting Changes**

**Cursor AI** changes:
- `backend/change-jwt-format` (changes token structure)

**Claude Code** assumes:
- Old JWT format in `AuthInterceptor.kt`

**Resolution:**
1. **You coordinate:** "Cursor changed JWT format, Claude needs to update"
2. Claude creates: `fix/update-jwt-parsing`
3. Test both branches together before merging
4. Merge backend first, then frontend fix

## 📝 Commit Message Format

Use **Conventional Commits** format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### **Types:**
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code restructuring
- `docs`: Documentation only
- `test`: Adding tests
- `chore`: Build/dependency updates

### **Scopes:**
- `android`: Android app
- `backend`: Backend services
- `user-service`: Specific microservice
- `task-service`: Specific microservice
- `api`: API changes
- `auth`: Authentication

### **Examples:**

```bash
# Good commit messages
git commit -m "feat(android): Add pull-to-refresh for task list"
git commit -m "fix(backend): Resolve JWT expiration bug"
git commit -m "refactor(task-service): Optimize database queries"
git commit -m "docs(api): Update OpenAPI specification"

# Bad commit messages (avoid)
git commit -m "updates"
git commit -m "fix bug"
git commit -m "work in progress"
```

## 🚀 Quick Reference Commands

### **Check Current Branch**
```bash
git branch  # Show local branches
git branch -r  # Show remote branches
git status  # Show current state
```

### **Switch Branches**
```bash
git checkout main
git checkout feature/android-app
git checkout -b new-branch-name  # Create and switch
```

### **Update from Main**
```bash
git checkout feature/android-app
git pull origin main  # Pull main changes into feature branch
```

### **View Commit History**
```bash
git log --oneline --graph --all  # Visual branch history
git log --oneline -10  # Last 10 commits
```

### **Undo Changes (before commit)**
```bash
git restore <file>  # Discard changes
git restore --staged <file>  # Unstage file
```

### **Undo Commit (after commit)**
```bash
git reset --soft HEAD~1  # Undo last commit, keep changes
git reset --hard HEAD~1  # Undo last commit, discard changes ⚠️
```

## 🔍 Example: Full Feature Development

**Scenario:** Add task filtering by status

### **Backend Work (Cursor AI)**

```bash
# Day 1: Cursor AI
git checkout main
git pull origin main
git checkout -b backend/task-filtering

# Add filtering logic to TaskController.java
# Commit
git add backend/task-service/
git commit -m "feat(backend): Add task filtering by status

- Add filterByStatus query parameter to GET /api/tasks
- Support multiple status values (PENDING,IN_PROGRESS)
- Update TaskRepository with filtering logic
- Add unit tests for filtering

Example: GET /api/tasks?userId=1&filterByStatus=PENDING,IN_PROGRESS"

git push -u origin backend/task-filtering
```

### **Frontend Work (Claude Code)**

```bash
# Day 2: Claude Code (after backend is ready)
git checkout main
git pull origin main
git checkout -b feature/android-task-filters

# Add filter UI to TasksScreen.kt
# Update TasksViewModel to use new API
# Commit
git add android-app/
git commit -m "feat(android): Add task status filter UI

- Add filter chips for PENDING/IN_PROGRESS/COMPLETED
- Update TaskApiService to support filterByStatus parameter
- Add filter state to TasksViewModel
- Persist selected filters in ViewModel

Depends on: backend/task-filtering"

git push -u origin feature/android-task-filters
```

### **Integration**

```bash
# Day 3: Test together
git checkout main

# Merge backend first
git merge backend/task-filtering
git push origin main

# Merge frontend
git merge feature/android-task-filters
git push origin main

# Delete merged branches
git branch -d backend/task-filtering feature/android-task-filters
git push origin --delete backend/task-filtering feature/android-task-filters
```

## 🎯 Best Practices

### ✅ DO:

1. **Always start from `main`:**
   ```bash
   git checkout main && git pull origin main
   ```

2. **Use descriptive branch names:**
   - Good: `feature/android-offline-sync`
   - Bad: `temp`, `test`, `fix`

3. **Commit often, push often:**
   ```bash
   git add . && git commit -m "..." && git push
   ```

4. **Test before merging:**
   - Run all tests
   - Verify API integration

5. **Keep branches short-lived:**
   - Merge within 1-3 days
   - Avoid long-running feature branches

### ❌ DON'T:

1. **Don't commit to `main` directly:**
   ```bash
   # ❌ Bad
   git checkout main
   git add .
   git commit -m "quick fix"
   ```

2. **Don't merge without testing:**
   - Always test backend + frontend together

3. **Don't mix frontend and backend in one branch:**
   - Keep `feature/` for frontend only
   - Keep `backend/` for backend only

4. **Don't leave branches unmerged for weeks:**
   - Causes merge conflicts
   - Hard to review

## 🆘 Troubleshooting

### **Merge Conflict**

```bash
# During merge, Git shows conflict
git merge backend/add-priority
# CONFLICT in android-app/app/src/main/java/com/microtodo/android/data/remote/dto/TaskDtos.kt

# Open file and resolve conflicts (look for <<<<<<, ======, >>>>>>)
# After resolving:
git add android-app/app/src/main/java/com/microtodo/android/data/remote/dto/TaskDtos.kt
git commit -m "merge: Resolve conflict in TaskDtos.kt"
```

### **Wrong Branch**

```bash
# Committed to wrong branch
git log  # Find commit hash (e.g., abc123)
git checkout correct-branch
git cherry-pick abc123  # Apply commit to correct branch

# Remove from wrong branch
git checkout wrong-branch
git reset --hard HEAD~1  # Remove last commit
```

### **Need to Update Branch from Main**

```bash
# Your branch is behind main
git checkout feature/android-app
git pull origin main  # Pull latest main changes
# Resolve any conflicts
git push origin feature/android-app
```

## 📊 Visual Workflow

```
Initial State:
    main
     │

Claude creates branch:
    main
     │
     └── feature/android-app (Claude working here)

Cursor creates branch:
    main
     │
     ├── feature/android-app
     └── backend/add-priority (Cursor working here)

After testing both:
    main
     │
     ├── feature/android-app ──┐
     └── backend/add-priority ─┘
                               │
                               ▼
    main (merged both branches)
     │
```

## 🔗 Useful GitHub Links

- **Repository:** https://github.com/SarpTekin/aws-springboot-todo-app
- **Pull Requests:** https://github.com/SarpTekin/aws-springboot-todo-app/pulls
- **Branches:** https://github.com/SarpTekin/aws-springboot-todo-app/branches
- **Current Android PR:** https://github.com/SarpTekin/aws-springboot-todo-app/pull/new/feature/android-app

---

## 📌 Summary

**Golden Rules:**
1. **One branch per feature/fix**
2. **Frontend = `feature/` prefix**
3. **Backend = `backend/` prefix**
4. **Always start from `main`**
5. **Test before merging**
6. **Keep branches short-lived**
7. **Use descriptive commit messages**
8. **Coordinate API changes between AIs**

**Your role:**
- Coordinate between Claude and Cursor
- Inform each AI about the other's changes
- Decide merge order (usually backend first)
- Test integrated system before merging to `main`

---

**Next Steps:**
1. ✅ Android app committed to `feature/android-app`
2. 🔲 You can now ask Cursor AI to work on backend improvements
3. 🔲 When Cursor makes changes, inform me so I can adapt the Android app
4. 🔲 After testing both, merge to `main`
