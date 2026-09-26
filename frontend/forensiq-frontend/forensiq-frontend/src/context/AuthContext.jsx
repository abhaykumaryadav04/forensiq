import { createContext, useContext, useState, useCallback, useMemo } from 'react'

// The ForensiQ backend protects every endpoint (except /actuator/health)
// with HTTP Basic auth. We never persist credentials to localStorage.
// sessionStorage is used only so a page refresh doesn't force re-entry
// mid-session; closing the tab clears it.
const SESSION_KEY = 'forensiq.basicAuth'

const AuthContext = createContext(null)

function readStoredCredentials() {
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [credentials, setCredentialsState] = useState(readStoredCredentials)

  const setCredentials = useCallback((username, password) => {
    const value = { username, password }
    setCredentialsState(value)
    try {
      window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(value))
    } catch {
      // sessionStorage unavailable (private browsing, etc.) — keep in memory only
    }
  }, [])

  const clearCredentials = useCallback(() => {
    setCredentialsState(null)
    try {
      window.sessionStorage.removeItem(SESSION_KEY)
    } catch {
      // ignore
    }
  }, [])

  const value = useMemo(
    () => ({ credentials, setCredentials, clearCredentials, isConfigured: !!credentials }),
    [credentials, setCredentials, clearCredentials]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
