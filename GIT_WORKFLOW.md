# EventfindR Git Workflow Guide

**Date:** 2026-05-06  
**Status:** ✅ Complete

---

## 📋 Summary

Your repository has been successfully synced between GitHub (origin) and GitLab (gitlab) remotes:

- **GitHub (origin):** `https://github.com/FedoHUN/EventFindR.git`
- **GitLab (gitlab):** `https://gitlab.fullstackacademy.sk/fsa-vojtko/eventfindr-backend.git`

### Current state:
- ✅ Local branch: `main` at commit `ac94a55`
- ✅ GitLab: `main` pushed successfully
- ✅ GitHub: `origin/main` at `7e19785` (you are 2 commits ahead)
- ✅ `.gitignore` updated to exclude AI-generated docs and example files

---

## 🔄 Git Workflow for Future Development

### Standard workflow (push to GitLab):

```powershell
# 1. Make your changes
# (edit files in fsa-eventfindr-backend or fsa-eventfindr-frontend)

# 2. Check status
git status

# 3. Stage changes
git add .

# 4. Commit
git commit -m "Brief description of changes"

# 5. Push to GitLab
git push gitlab main

# OR push to GitHub (if you want to keep it in sync)
git push origin main
```

### If you get "non-fast-forward" error:

```powershell
# 1. Fetch from both remotes
git fetch origin
git fetch gitlab

# 2. Rebase your local branch
git rebase origin/main
# OR
git rebase gitlab/main

# 3. If conflicts, resolve them:
git add .
git rebase --continue

# 4. Push with force-with-lease (safe force push)
git push gitlab main --force-with-lease
```

### Syncing between GitHub and GitLab:

```powershell
# Keep local updated from GitHub
git fetch origin
git merge origin/main

# Push to GitLab
git push gitlab main
```

---

## 📝 What Changed in .gitignore

Added these patterns to exclude non-production files:

```
# AI-generated and support documentation
DEVOPS-SETUP.md
DEVOPS-CHECKLIST.md
DEVOPS-INDEX.md
DEVOPS-MANIFEST.md
GET-STARTED.md
INDEX.md
JAVA_VERSION_ALIGNMENT.md
QUICKSTART.md
READY.md
TLDR.md
README-attendance-toggle.md

# Example projects (reference only)
example/
```

This keeps your repository clean by:
- Excluding generated/reference docs
- Excluding the `/example` folder (FSA reference projects)
- Keeping only real application code

---

## 🔑 Important: Two Remotes

You have **two push destinations**:

| Remote | URL | Purpose |
|--------|-----|---------|
| `origin` | https://github.com/FedoHUN/EventFindR.git | Personal GitHub repo |
| `gitlab` | https://gitlab.fullstackacademy.sk/fsa-vojtko/eventfindr-backend.git | School GitLab (CI/CD pipeline) |

**CI/CD Pipeline runs on GitLab**, so always push to `gitlab main` for deployment.

### View your remotes:

```powershell
git remote -v
```

Output should be:
```
gitlab  https://gitlab.fullstackacademy.sk/fsa-vojtko/eventfindr-backend.git (fetch)
gitlab  https://gitlab.fullstackacademy.sk/fsa-vojtko/eventfindr-backend.git (push)
origin  https://github.com/FedoHUN/EventFindR.git (fetch)
origin  https://github.com/FedoHUN/EventFindR.git (push)
```

---

## ✅ Checklist for Future Commits

- [ ] Edit code in `fsa-eventfindr-backend/` or `fsa-eventfindr-frontend/`
- [ ] `git status` shows your changes (not example/ or AI docs)
- [ ] `git add .` stages only real code
- [ ] `git commit -m "Clear description"` creates commit
- [ ] `git push gitlab main` pushes to GitLab for CI/CD
- [ ] Check GitLab Runners to see build status: https://gitlab.fullstackacademy.sk/fsa-vojtko

---

## 🐛 Troubleshooting

### Problem: "non-fast-forward" error on push

**Solution:**

```powershell
git pull --rebase gitlab main
git push gitlab main
```

### Problem: Accidentally committed AI docs or example files

**Solution:**

```powershell
# Check what's staged
git status

# If files are in staging area, reset them
git reset HEAD FILENAME

# OR remove them from last commit
git reset --soft HEAD~1
git reset HEAD FILENAME
git commit -m "Commit message"
git push gitlab main --force-with-lease
```

### Problem: Branch tracking is wrong

**Solution:**

```powershell
# Set upstream to GitLab
git branch -u gitlab/main

# Verify
git branch -vv
```

---

## 📚 Useful Commands

```powershell
# Show commit history (pretty)
git log --oneline --decorate --graph -10

# Show status
git status -sb

# Show differences
git diff

# Show staged changes
git diff --staged

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Undo last commit (discard changes)
git reset --hard HEAD~1

# Clean untracked files
git clean -fd
```

---

## 🎯 Key Points to Remember

1. **Always push to GitLab** (`git push gitlab main`) — that's where CI/CD runs
2. **GitHub** is optional for backup — you can keep it synced for redundancy
3. **`.gitignore` matters** — it keeps the repo clean and deployment fast
4. **Rebase instead of merge** when pulling — keeps history cleaner
5. **Use `--force-with-lease`** instead of `--force` — safer forced push

---

**Questions?** Check the troubleshooting section or review the DEVOPS-SETUP.md for deployment steps.


