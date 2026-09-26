import { useState } from 'react'
import { Routes, Route } from 'react-router-dom'
import Sidebar from './components/Layout/Sidebar.jsx'
import Header from './components/Layout/Header.jsx'
import { useApiAuthSync } from './hooks/useApiAuthSync.js'
import Dashboard from './pages/Dashboard.jsx'
import NewScan from './pages/NewScan.jsx'
import ScanResult from './pages/ScanResult.jsx'
import ScanHistory from './pages/ScanHistory.jsx'
import Reports from './pages/Reports.jsx'
import Audit from './pages/Audit.jsx'
import Settings from './pages/Settings.jsx'

export default function App() {
  useApiAuthSync()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="flex min-h-screen bg-steel-50">
      <Sidebar open={sidebarOpen} onNavigate={() => setSidebarOpen(false)} />
      {sidebarOpen && (
        <button
          type="button"
          aria-label="Close navigation menu"
          className="fixed inset-0 z-30 bg-ink-950/40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
      <div className="flex min-h-screen flex-1 flex-col lg:pl-0">
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-6xl">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/new-scan" element={<NewScan />} />
              <Route path="/result/:documentId" element={<ScanResult />} />
              <Route path="/history" element={<ScanHistory />} />
              <Route path="/reports" element={<Reports />} />
              <Route path="/audit" element={<Audit />} />
              <Route path="/settings" element={<Settings />} />
            </Routes>
          </div>
        </main>
      </div>
    </div>
  )
}
