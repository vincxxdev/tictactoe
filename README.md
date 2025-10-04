# 🎮 Tic Tac Toe Online

A real-time multiplayer Tic Tac Toe game built with React (TypeScript) and Spring Boot, featuring WebSocket communication for instant gameplay.

## ✨ Features

- **Real-time Multiplayer**: Play against opponents in real-time using WebSockets
- **Random Matchmaking**: Join random games or create private rooms
- **Surrender System**: Request and respond to surrender offers
- **Auto-cleanup**: Automatic cleanup of old and abandoned games
- **Responsive UI**: Modern, beautiful interface with Tailwind CSS
- **Type-safe**: Full TypeScript support on the frontend
- **Comprehensive Testing**: 69 tests covering all major functionality

## 🏗️ Architecture

### Backend (Spring Boot)
- **Redis Storage**: Distributed, persistent game state storage
- **WebSocket Communication**: STOMP over SockJS for real-time messaging
- **Bean Validation**: Input validation on all endpoints
- **Global Exception Handling**: Consistent error responses
- **Scheduled Cleanup**: Automatic removal of stale games
- **Horizontal Scalability**: Redis allows multiple backend instances

### Frontend (React + TypeScript)
- **Context API**: Centralized state management
- **Error Boundaries**: Graceful error handling
- **Toast Notifications**: User-friendly error messages
- **Environment Configuration**: Easy deployment to different environments

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Running Tests](#-running-tests)
- [Production Build](#-production-build)
- [Docker Deployment](#-docker-deployment)
- [Configuration](#-configuration)
- [Environment Variables](#-environment-variables-setup)
- [Project Structure](#-project-structure)
- [How to Play](#-how-to-play)
- [Development Tips](#-development-tips)
- [Troubleshooting](#-troubleshooting)
- [API Endpoints](#-api-endpoints)
- [Security](#-security-considerations)
- [Future Improvements](#-future-improvements)

## 🚀 Quick Start

### Prerequisites
- **Java 17** or higher
- **Node.js 16** or higher
- **Maven 3.6** or higher
- **Redis** (optional for local development, required for production)

### 🎯 First Time Setup (5 minutes)

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd tictactoe
```

#### 2. Create Frontend Environment Files (Optional)

The application works out of the box with default settings. If you need to customize the backend URL, create these files:

```bash
# Create .env.development (optional)
cat > frontend/.env.development << 'EOF'
# Development Environment
REACT_APP_API_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
REACT_APP_ENV=development
EOF

# Create .env.production (optional, for production builds)
cat > frontend/.env.production << 'EOF'
# Production Environment
# These should be overridden at build time
REACT_APP_API_URL=
REACT_APP_WS_URL=
REACT_APP_ENV=production
EOF
```

#### 3. Start Redis (Development)

```bash
# Using Docker (recommended)
docker run -d -p 6379:6379 --name tictactoe-redis redis:7-alpine

# Or install Redis locally
# macOS: brew install redis && redis-server
# Ubuntu: sudo apt install redis-server && sudo systemctl start redis
# Windows: Use Redis for Windows or Docker
```

#### 4. Install Dependencies

```bash
# Backend
cd backend
./mvnw clean install
cd ..

# Frontend
cd frontend
npm install
cd ..
```

### ⚡ Run the Application

#### Option 1: Development Mode (Recommended for development)

**Terminal 1 - Backend:**
```bash
cd backend
./mvnw spring-boot:run
```
Wait for "Started TictactoeApplication" message (Backend runs on `http://localhost:8080`)

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```
Browser will open automatically at `http://localhost:3000`

#### Option 2: Docker (Recommended for testing production build)

```bash
# Build and run with docker-compose
docker-compose up --build

# Or run in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

Access at: `http://localhost`

### 🎮 Play the Game

1. **Open two browser tabs/windows** at http://localhost:3000
2. **Tab 1**: Click "Create New Game" → Copy the Game ID
3. **Tab 2**: Paste Game ID and click "Join Game by ID"
4. **Start playing!** X goes first

Or use "Join Random Game" in both tabs for automatic matchmaking.

### 🔍 Verify Everything Works

#### Check Backend Health
```bash
curl http://localhost:8080/api/health
```
Expected response:
```json
{
  "status": "UP",
  "timestamp": 1234567890,
  "service": "tictactoe"
}
```

#### Check Game Stats
```bash
curl http://localhost:8080/api/stats
```

#### Check Frontend
Open http://localhost:3000 - you should see the Tic Tac Toe lobby

## 🧪 Running Tests

### Backend Tests
```bash
cd backend
./mvnw test
```

**Run specific test:**
```bash
./mvnw test -Dtest=GameServiceTest
```

### Frontend Tests
```bash
cd frontend
npm test
```

Run once (non-interactive):
```bash
npm test -- --watchAll=false
```

**Run tests in watch mode:**
```bash
npm test
```

## 📦 Production Build

### Backend
```bash
cd backend
./mvnw clean package
java -jar target/tictactoe-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Clean and rebuild:**
```bash
./mvnw clean package
```

### Frontend
```bash
cd frontend
npm run build
```

The production build will be in the `build/` directory and can be served with any static file server.

**Analyze bundle size:**
```bash
npm run build
npx source-map-explorer 'build/static/js/*.js'
```

## 🐳 Docker Deployment

### Build Images
```bash
# Backend
cd backend
docker build -t tictactoe-backend:latest .

# Frontend
cd frontend
docker build -t tictactoe-frontend:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## 🔧 Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties` or set environment variables:

```properties
# Server Port
SERVER_PORT=8080

# CORS Origins (comma-separated)
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# WebSocket Origins (comma-separated)
WEBSOCKET_ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Game cleanup settings
GAME_CLEANUP_INTERVAL=30
GAME_MAX_IDLE_TIME=60
```

### Frontend Configuration

See the [Environment Variables Setup](#-environment-variables-setup) section below for detailed frontend configuration.

## 🌍 Environment Variables Setup

### Frontend Environment Files

Create the following files in the `frontend/` directory:

#### `.env.example`
```env
# Backend API URL
REACT_APP_API_URL=http://localhost:8080

# WebSocket URL
REACT_APP_WS_URL=http://localhost:8080/ws

# Environment
REACT_APP_ENV=development
```

#### `.env.development`
```env
# Development Environment
REACT_APP_API_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
REACT_APP_ENV=development
```

#### `.env.production`
```env
# Production Environment
# These should be overridden at build time
REACT_APP_API_URL=
REACT_APP_WS_URL=
REACT_APP_ENV=production
```

### For Production Deployment

Create `.env.production.local` in `frontend/` with your actual URLs:

```env
REACT_APP_API_URL=https://your-backend-domain.com
REACT_APP_WS_URL=https://your-backend-domain.com/ws
REACT_APP_ENV=production
```

## 📁 Project Structure

```
tictactoe/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tictactoe/
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── exception/       # Custom exceptions & handlers
│   │   │   │   ├── model/           # Data models
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── storage/         # In-memory storage
│   │   │   │   ├── GameController.java    # WebSocket endpoints
│   │   │   │   ├── WebSocketConfig.java   # WebSocket configuration
│   │   │   │   └── TictactoeApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── application-prod.properties
│   │   └── test/                    # Unit & integration tests
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   ├── components/              # React components
    │   ├── contexts/                # Context providers
    │   ├── services/                # WebSocket service
    │   ├── utils/                   # Utility functions
    │   ├── config/                  # Configuration
    │   ├── __tests__/               # Test files
    │   └── App.tsx
    ├── public/
    └── package.json
```

## 🎮 How to Play

1. **Start a Game**:
   - Click "Create New Game" to start a new room
   - Share the Game ID with a friend
   - Or click "Join Random Game" to find an opponent

2. **Gameplay**:
   - Players alternate turns (X goes first)
   - Click an empty square to make your move
   - First player to get 3 in a row wins
   - Game ends in a draw if all squares are filled

3. **Surrender**:
   - Click "Surrender" button if you want to forfeit
   - Opponent can accept or decline your surrender

## 🛠️ Development Tips

### Backend

**Watch for changes** (use Spring Boot DevTools):
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

**Run specific test:**
```bash
./mvnw test -Dtest=GameServiceTest
```

**Clean and rebuild:**
```bash
./mvnw clean package
```

### Frontend

**Run tests in watch mode:**
```bash
npm test
```

**Build for production:**
```bash
npm run build
```

**Analyze bundle size:**
```bash
npm run build
npx source-map-explorer 'build/static/js/*.js'
```

## 🐛 Troubleshooting

### Port Already in Use

**Backend (8080):**
```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Frontend (3000):**
```bash
# macOS/Linux
lsof -ti:3000 | xargs kill -9

# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### Redis Connection Failed

```bash
# Check if Redis is running
redis-cli ping
# Expected response: PONG

# Start Redis with Docker
docker start tictactoe-redis

# Check Redis logs
docker logs tictactoe-redis
```

### WebSocket Connection Failed

1. Ensure backend is running on port 8080
2. Ensure Redis is running and accessible
3. Check browser console for errors
4. Verify CORS settings in `application.properties`
5. Try in incognito mode to rule out browser extensions
6. Check CORS/WebSocket origins configuration
7. Verify firewall rules allow WebSocket connections
8. Ensure HTTPS is used in production (wss:// not ws://)

### Docker Build Fails

```bash
# Clean everything and rebuild
docker-compose down -v
docker system prune -a
docker-compose up --build
```

### Frontend Won't Start

```bash
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
npm start
```

### Backend Won't Start

1. Verify Java 17+ is installed: `java -version`
2. Check port 8080 is available: `netstat -an | grep 8080`
3. Review application logs for startup errors
4. Clean and rebuild:
```bash
cd backend
./mvnw clean
rm -rf target
./mvnw spring-boot:run
```

### Frontend Build Fails

1. Delete node_modules and reinstall: `rm -rf node_modules && npm install`
2. Clear npm cache: `npm cache clean --force`
3. Check Node.js version: `node -v` (should be 16+)
4. Verify environment variables are set correctly

## 📄 API Endpoints

### REST Endpoints
- `GET /api/health` - Health check
- `GET /api/stats` - Game statistics

### WebSocket Endpoints
- `/app/game.start` - Start a new game
- `/app/game.connect` - Connect to a game
- `/app/game.gameplay` - Make a move
- `/app/game.surrender` - Request surrender
- `/app/game.surrender.response` - Respond to surrender

### WebSocket Topics
- `/topic/game.created/{playerLogin}` - Game created notification
- `/topic/game.connected/{playerLogin}` - Player connected notification
- `/topic/game.{gameId}` - Game state updates

## 🔒 Security Considerations

### Current Security Features
- **Input Validation**: All user inputs are validated on the backend
- **CORS Configuration**: Configurable allowed origins
- **Error Handling**: No sensitive information exposed in errors
- **Rate Limiting**: Consider adding rate limiting for production

### Security Hardening

#### Backend
- Use HTTPS in production
- Enable Spring Security
- Implement rate limiting
- Add authentication/authorization
- Keep dependencies updated

#### Frontend
- Use environment-specific API keys
- Implement Content Security Policy
- Enable HTTPS only
- Sanitize user inputs
- Keep dependencies updated

## 🚀 Future Improvements

- [x] ~~Add Redis for distributed storage~~
- [ ] Implement user authentication
- [ ] Add game history and statistics (persist to database)
- [ ] Implement matchmaking with ELO rating
- [ ] Add chat functionality
- [ ] Support for different board sizes
- [ ] Mobile app with React Native
- [ ] Add animations and sound effects
- [ ] Add Pub/Sub for multi-instance WebSocket synchronization
- [ ] Add CI/CD pipeline
- [ ] Implement rate limiting
- [ ] Add comprehensive monitoring

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

Built with ❤️ using Spring Boot and React

---

**Note**: This is a learning project. For production use, consider adding:
- Database persistence (PostgreSQL, MongoDB)
- Redis for session management
- Authentication & Authorization
- Rate limiting
- Monitoring & Logging (ELK Stack, Prometheus)
- CI/CD pipeline
