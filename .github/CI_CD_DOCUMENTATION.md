# CI/CD Pipeline Documentation

This document describes the Continuous Integration and Continuous Deployment (CI/CD) pipelines configured for the TicTacToe application.

## Overview

The project uses GitHub Actions for automated testing, building, and deployment. The CI/CD setup consists of several workflows that handle different aspects of the development lifecycle.

## Workflows

### 1. Frontend CI (`frontend-ci.yml`)

**Trigger:** Push or Pull Request to `main` or `develop` branches (when frontend files change)

**Purpose:** Validates frontend code quality and builds the application

**Jobs:**
- **build-and-test**: Runs on Node.js 18.x and 20.x
  - Installs dependencies
  - Runs ESLint for code quality
  - Executes tests with coverage reporting
  - Builds the production bundle
  - Uploads coverage to Codecov
  - Stores build artifacts

- **docker-build**: Validates Docker image creation
  - Builds the frontend Docker image
  - Uses layer caching for faster builds

### 2. Backend CI (`backend-ci.yml`)

**Trigger:** Push or Pull Request to `main` or `develop` branches (when backend files change)

**Purpose:** Validates backend code quality and builds the application

**Jobs:**
- **build-and-test**: Runs on Java 17 and 21
  - Compiles the Maven project
  - Runs unit tests
  - Generates JaCoCo coverage reports
  - Packages the JAR file
  - Uploads coverage to Codecov
  - Stores JAR artifacts

- **security-scan**: Security vulnerability scanning
  - Runs OWASP Dependency Check
  - Generates security reports

- **docker-build**: Validates Docker image creation
  - Builds the backend Docker image
  - Uses layer caching for faster builds

### 3. Continuous Deployment (`cd.yml`)

**Trigger:** Push to `main` branch or version tags (`v*.*.*`)

**Purpose:** Builds and publishes Docker images to GitHub Container Registry

**Jobs:**
- **build-and-push-frontend**:
  - Runs full test suite
  - Builds production-ready frontend
  - Creates and pushes Docker image to GHCR
  - Tags images with version, branch, and SHA

- **build-and-push-backend**:
  - Runs full test suite
  - Packages Spring Boot application
  - Creates and pushes Docker image to GHCR
  - Tags images with version, branch, and SHA

- **deploy**:
  - Placeholder for deployment automation
  - Can be configured for your specific infrastructure

### 4. Integration Tests (`integration-tests.yml`)

**Trigger:** Push, Pull Request, and weekly schedule (Sunday midnight)

**Purpose:** Validates the entire application stack

**Jobs:**
- **integration-test**:
  - Starts Redis service
  - Builds and runs backend
  - Builds frontend
  - Runs integration tests

- **docker-compose-test**:
  - Starts all services using Docker Compose
  - Validates service health
  - Ensures proper service communication

### 5. Code Quality (`code-quality.yml`)

**Trigger:** Push or Pull Request to `main` or `develop` branches

**Purpose:** Ensures code quality and security standards

**Jobs:**
- **codeql-analysis**:
  - Scans JavaScript/TypeScript and Java code
  - Identifies security vulnerabilities
  - Reports findings in Security tab

- **dependency-review** (PR only):
  - Reviews dependency changes
  - Flags vulnerable or problematic dependencies

- **lighthouse**:
  - Runs performance audits
  - Checks accessibility, best practices, and SEO

### 6. Pull Request Checks (`pr-checks.yml`)

**Trigger:** Pull Request to `main` or `develop` branches

**Purpose:** Validates pull request quality

**Jobs:**
- **pr-validation**:
  - Validates PR title follows semantic conventions
  - Checks for merge conflicts
  - Validates commit messages

- **size-label**:
  - Automatically labels PRs by size (xs, s, m, l, xl)

- **auto-assign**:
  - Automatically assigns reviewers based on configuration

### 7. Release Management (`release.yml`)

**Trigger:** Push of version tags (`v*.*.*`)

**Purpose:** Creates GitHub releases with artifacts

**Jobs:**
- **create-release**:
  - Generates changelog
  - Creates GitHub release
  - Documents Docker image tags

- **build-release-artifacts**:
  - Builds JAR and frontend bundle
  - Attaches artifacts to release

## Setup Instructions

### 1. Enable GitHub Actions

GitHub Actions should be enabled by default. Verify in repository Settings → Actions.

### 2. Configure Secrets

No additional secrets are required for basic CI/CD. For deployment, you may need:

- `DEPLOY_KEY`: SSH key for deployment
- `DEPLOY_HOST`: Deployment server address
- Custom cloud provider credentials (AWS, Azure, GCP)

### 3. Configure Branch Protection

Recommended settings for `main` branch:
- Require pull request reviews
- Require status checks to pass:
  - `build-and-test (frontend)`
  - `build-and-test (backend)`
  - `integration-test`
  - `CodeQL Analysis`
- Require branches to be up to date
- Require conversation resolution

### 4. Enable Container Registry

GitHub Container Registry (GHCR) is enabled by default. Images are published to:
- `ghcr.io/<owner>/<repo>/frontend`
- `ghcr.io/<owner>/<repo>/backend`

### 5. Configure Auto-assign Reviewers

Edit `.github/auto-assign.yml` and add team member usernames:

```yaml
reviewers:
  - username1
  - username2
  - username3
```

## Commit Message Convention

This project follows the Conventional Commits specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Test additions or changes
- `build`: Build system changes
- `ci`: CI/CD changes
- `chore`: Other changes
- `revert`: Revert a previous commit

**Examples:**
```
feat(frontend): add player statistics dashboard
fix(backend): resolve game state synchronization issue
docs: update deployment guide
ci: add Docker layer caching
```

## Deployment

### Manual Deployment

1. Tag a new version:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

2. GitHub Actions will:
   - Run all tests
   - Build Docker images
   - Push to GHCR
   - Create GitHub release
   - Attach artifacts

### Pull Docker Images

```bash
# Pull frontend
docker pull ghcr.io/<owner>/<repo>/frontend:v1.0.0

# Pull backend
docker pull ghcr.io/<owner>/<repo>/backend:v1.0.0
```

### Deploy with Docker Compose

Update `docker-compose.prod.yml` with the version tag:

```yaml
services:
  frontend:
    image: ghcr.io/<owner>/<repo>/frontend:v1.0.0
  
  backend:
    image: ghcr.io/<owner>/<repo>/backend:v1.0.0
```

Then deploy:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring and Maintenance

### View Workflow Runs

- Go to repository → Actions tab
- Click on a workflow to see runs
- Click on a run to see job details and logs

### Coverage Reports

Coverage reports are uploaded to Codecov (if configured). You can also view them in workflow artifacts:
- Frontend: `frontend-coverage`
- Backend: `backend-coverage`

### Security Alerts

- CodeQL findings: Security tab → Code scanning alerts
- Dependency alerts: Security tab → Dependabot alerts
- OWASP reports: Download from workflow artifacts

### Failed Builds

When a build fails:
1. Check the workflow run logs
2. Identify the failing job and step
3. Review error messages
4. Fix the issue locally
5. Push the fix

## Best Practices

1. **Always create pull requests** - Don't push directly to `main`
2. **Write meaningful commit messages** - Follow conventional commits
3. **Keep PRs small** - Easier to review and test
4. **Wait for CI checks** - Don't merge until all checks pass
5. **Update tests** - Add tests for new features
6. **Monitor coverage** - Maintain or improve code coverage
7. **Review security alerts** - Address vulnerabilities promptly
8. **Use semantic versioning** - Follow semver for releases

## Troubleshooting

### "Build failing on main but passing locally"

- Ensure dependencies are properly locked (package-lock.json, pom.xml)
- Check for environment-specific issues
- Review CI logs for specific error messages

### "Docker build timeout"

- Increase timeout in workflow
- Optimize Dockerfile
- Use multi-stage builds
- Enable layer caching

### "Integration tests failing"

- Check service startup order
- Verify health check endpoints
- Increase wait times for services
- Review service logs in workflow

### "Coverage upload failing"

- Verify coverage files are generated
- Check Codecov token if required
- Ensure coverage format is correct

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/actions)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

## Support

For issues or questions about the CI/CD setup:
1. Check workflow logs
2. Review this documentation
3. Open an issue in the repository
4. Contact the development team
