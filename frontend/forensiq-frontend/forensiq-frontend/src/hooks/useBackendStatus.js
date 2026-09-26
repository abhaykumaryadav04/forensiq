import { useEffect, useState, useCallback } from 'react'
import { checkBackendHealth } from '../services/screeningApi.js'

const POLL_INTERVAL_MS = 30000

export default function useBackendStatus() {
  const [status, setStatus] = useState('checking')

  const check = useCallback(async () => {
    try {
      const data = await checkBackendHealth()
      setStatus(data?.status === 'UP' ? 'online' : 'offline')
    } catch (error) {
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        setStatus('unauthorized')
      } else {
        setStatus('offline')
      }
    }
  }, [])

  useEffect(() => {
    check()
    const id = setInterval(check, POLL_INTERVAL_MS)
    return () => clearInterval(id)
  }, [check])

  return { status, recheck: check }
}
