import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { AuthProvider } from './api/AuthContext'; {/*추가 */}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  // <React.StrictMode>
  <AuthProvider> {/*추가 */}
    <App />
  </AuthProvider>
  // </React.StrictMode>
);