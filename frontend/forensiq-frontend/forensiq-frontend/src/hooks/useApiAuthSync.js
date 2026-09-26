import { useEffect } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { setApiCredentials } from '../services/api.js'

// Keeps the shared axios instance's Basic-auth credentials in sync
// with whatever is currently held in AuthContext.
export function useApiAuthSync() {
  const { credentials } = useAuth()
  useEffect(() => {
    setApiCredentials(credentials)
  }, [credentials])
}
