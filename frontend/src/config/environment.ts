const config = {
  apiUrl: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  wsUrl: process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws',
  environment: process.env.REACT_APP_ENV || 'development',
  isDevelopment: process.env.REACT_APP_ENV === 'development',
  isProduction: process.env.REACT_APP_ENV === 'production',
};

export default config;

