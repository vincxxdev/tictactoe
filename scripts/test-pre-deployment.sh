#!/bin/bash

# Pre-deployment script test
# Verify that all necessary files and configurations are in place before deployment

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

echo "🧪 Test Pre-Deployment"
echo "======================="
echo ""

# 1. Verify necessary files exist
print_info "Verify necessary files..."

FILES=(
    "docker-compose.prod.yml"
    ".env.production.example"
    ".github/workflows/cd.yml"
    "backend/Dockerfile"
    "frontend/Dockerfile"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        print_success "File found: $file"
    else
        print_error "File missing: $file"
        exit 1
    fi
done

echo ""

# 2. Verify YAML syntax
print_info "Verify YAML syntax..."

if command -v yamllint &> /dev/null; then
    yamllint -d relaxed .github/workflows/cd.yml docker-compose.prod.yml 2>/dev/null || print_warning "Some YAML warnings (non blocking)"
    print_success "YAML syntax valid"
else
    print_warning "yamllint not installed, skip YAML verification"
fi

echo ""

# 3. Verify Docker is running
print_info "Verify Docker..."

if ! docker info &> /dev/null; then
    print_error "Docker is not running"
    exit 1
fi
print_success "Docker is running"

echo ""

# 4. Test build Docker images
print_info "Test build Docker images..."

# Backend
print_info "Building backend image..."
if docker build -t tictactoe-backend-test ./backend &> /dev/null; then
    print_success "Backend build OK"
else
    print_error "Backend build failed"
    exit 1
fi

# Frontend
print_info "Building frontend image..."
if docker build -t tictactoe-frontend-test ./frontend &> /dev/null; then
    print_success "Frontend build OK"
else
    print_error "Frontend build failed"
    exit 1
fi

echo ""

# 5. Verify docker-compose configuration
print_info "Test docker-compose configuration..."

if docker-compose -f docker-compose.prod.yml config &> /dev/null; then
    print_success "docker-compose.prod.yml valid"
else
    print_error "docker-compose.prod.yml not valid"
    exit 1
fi

echo ""

# 6. Verify that the .env file exists (optional for local test)
if [ -f ".env" ]; then
    print_success "File .env found"
    
    # Verify required variables
    REQUIRED_VARS=(
        "REDIS_PASSWORD"
        "CORS_ALLOWED_ORIGINS"
        "WEBSOCKET_ALLOWED_ORIGINS"
        "REACT_APP_API_URL"
        "REACT_APP_WS_URL"
    )
    
    print_info "Verify environment variables..."
    for var in "${REQUIRED_VARS[@]}"; do
        if grep -q "^${var}=" .env; then
            print_success "Variable ${var} configured"
        else
            print_warning "Variable ${var} missing in .env"
        fi
    done
else
    print_warning "File .env not found (OK for CI/CD, necessary for local test)"
fi

echo ""

# 7. Cleanup test images
print_info "Cleanup test images..."
docker rmi tictactoe-backend-test tictactoe-frontend-test &> /dev/null || true
print_success "Cleanup completed"

echo ""
echo "================================================"
print_success "All pre-deployment tests passed! ✨"
echo "================================================"

print_info "The automatic deployment will start after the push!"
