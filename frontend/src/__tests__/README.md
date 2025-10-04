# Test Structure

This directory contains all the test files for the Tic Tac Toe frontend application.

## Directory Structure

```
src/__tests__/
├── components/          # Component tests
│   ├── Board.test.tsx
│   ├── Game.test.tsx
│   ├── Lobby.test.tsx
│   └── Square.test.tsx
├── contexts/           # Context tests
│   └── GameContext.test.tsx
├── utils/              # Utility tests
│   └── nameGenerator.test.ts
├── App.test.tsx        # Main App component test
└── README.md           # This file
```

## Test Organization

- **Components**: Tests for React components (Board, Game, Lobby, Square)
- **Contexts**: Tests for React Context providers and hooks
- **Utils**: Tests for utility functions and helpers
- **App**: Main application test

## Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests once
npm test -- --watchAll=false
```

## Test Coverage

The test suite covers:
- ✅ Component rendering and behavior
- ✅ User interactions (clicks, form inputs)
- ✅ State management and context
- ✅ WebSocket communication mocking
- ✅ Utility functions
- ✅ Error handling and edge cases

Total: **69 tests** covering all major functionality.
