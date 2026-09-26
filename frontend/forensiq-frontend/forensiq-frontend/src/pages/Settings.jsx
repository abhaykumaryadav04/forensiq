import { useState } from 'react'
import Card from '../components/common/Card.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import useBackendStatus from '../hooks/useBackendStatus.js'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export default function Settings() {
  const { credentials, setCredentials, clearCredentials } = useAuth()
  const { status, recheck } = useBackendStatus()
  const [username, setUsername] = useState(credentials?.username || '')
  const [password, setPassword] = useState(credentials?.password || '')
  const [saved, setSaved] = useState(false)

  function handleSubmit(e) {
    e.preventDefault()
    setCredentials(username, password)
    setSaved(true)
    recheck()
    setTimeout(() => setSaved(false), 2000)
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">Settings</h2>
        <p className="text-sm text-steel-500">Backend connection and authentication.</p>
      </div>

      <Card title="Backend">
        <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-steel-500">Base URL</dt>
            <dd className="mt-0.5 font-mono text-sm text-ink-900">{BASE_URL}</dd>
            <p className="mt-1 text-xs text-steel-500">Set via the VITE_API_BASE_URL environment variable at build time.</p>
          </div>
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-steel-500">Connection Status</dt>
            <dd className="mt-0.5 text-sm text-ink-900 capitalize">{status}</dd>
          </div>
        </dl>
      </Card>

      <Card
        title="Backend Credentials"
        subtitle="The ForensiQ backend requires HTTP Basic auth on every endpoint except /actuator/health."
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="username" className="mb-1 block text-xs font-medium text-steel-500">
              Username
            </label>
            <input
              id="username"
              type="text"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="focus-ring w-full max-w-sm rounded-md border border-steel-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-xs font-medium text-steel-500">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="focus-ring w-full max-w-sm rounded-md border border-steel-300 px-3 py-2 text-sm"
            />
          </div>
          <div className="flex items-center gap-3">
            <button
              type="submit"
              className="focus-ring rounded-md bg-accent px-4 py-2.5 text-sm font-medium text-white hover:bg-accent-dim"
            >
              Save Credentials
            </button>
            <button
              type="button"
              onClick={() => {
                clearCredentials()
                setUsername('Abhay kumar')
                setPassword('Abhay@128')
              }}
              className="focus-ring rounded-md border border-steel-300 bg-white px-4 py-2.5 text-sm font-medium text-ink-800 hover:bg-steel-50"
            >
              Clear
            </button>
            {saved && <span className="text-sm text-signal-low">Saved</span>}
          </div>
          <p className="text-xs text-steel-500">
            Credentials are kept in this tab's session storage only — never in localStorage, and never sent
            anywhere besides the Authorization header of requests to the backend above.
          </p>
        </form>
      </Card>

      <Card title="Known Limitations" className="border-signal-review/30">
        <ul className="space-y-2 text-sm text-ink-700">
          <li>
            The backend does not currently expose a CORS configuration. If this frontend is served from a different
            origin than {BASE_URL}, browser requests may be blocked until CORS is configured on the backend.
          </li>
          <li>
            Scan History and the Audit page show only scans performed in this browser session — the backend doesn't
            yet provide a listing endpoint for either.
          </li>
          <li>
            Component risk breakdown only appears when the backend includes <code className="font-mono">componentRiskScores</code> in
            a response.
          </li>
        </ul>
      </Card>
    </div>
  )
}
