import { createContext, useContext, useState, useCallback, useMemo } from 'react'

// ForensiQ's backend does not yet expose a list endpoint for scan
// history or the audit trail (only POST /api/v1/screenigs/{id}/camera
// and GET /api/v1/reports/{documentId} exist today). Rather than invent
// data, we keep a client-side log of the real responses the backend has
// returned during this browser session, so Scan History / Audit have
// something genuine to show while that endpoint is pending.
const STORAGE_KEY = 'forensiq.sessionScans'

function readStored() {
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

const ScanHistoryContext = createContext(null)

export function ScanHistoryProvider({ children }) {
  const [scans, setScans] = useState(readStored)

  const recordScan = useCallback((entry) => {
    setScans((prev) => {
      const next = [{ ...entry, recordedAt: new Date().toISOString() }, ...prev]
      try {
        window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next))
      } catch {
        // ignore quota / privacy-mode errors
      }
      return next
    })
  }, [])

  const clear = useCallback(() => {
    setScans([])
    try {
      window.sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      // ignore
    }
  }, [])

  const value = useMemo(() => ({ scans, recordScan, clear }), [scans, recordScan, clear])

  return <ScanHistoryContext.Provider value={value}>{children}</ScanHistoryContext.Provider>
}

export function useScanHistory() {
  const ctx = useContext(ScanHistoryContext)
  if (!ctx) throw new Error('useScanHistory must be used within ScanHistoryProvider')
  return ctx
}
