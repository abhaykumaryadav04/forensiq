import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { ScanHistoryProvider } from './context/ScanHistoryContext.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <ScanHistoryProvider>
          <App />
        </ScanHistoryProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)
