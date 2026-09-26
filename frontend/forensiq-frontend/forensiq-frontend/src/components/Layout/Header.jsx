import { useAuth } from '../../context/AuthContext.jsx'
import useBackendStatus from '../../hooks/useBackendStatus.js'

export default function Header({ onMenuClick }) {
  const { status } = useBackendStatus()
  const { isConfigured } = useAuth()

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-steel-200 bg-white/95 px-4 backdrop-blur sm:px-6">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuClick}
          className="focus-ring rounded-md p-2 text-steel-600 hover:bg-steel-100 lg:hidden"
          aria-label="Open navigation menu"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
            <path d="M4 6h16M4 12h16M4 18h16" strokeLinecap="round" />
          </svg>
        </button>
        <div>
          <h1 className="text-sm font-semibold text-ink-900">Identity Document Screening</h1>
          <p className="hidden text-xs text-steel-500 sm:block">Explainable, backend-verified document analysis</p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <ConnectionBadge status={status} />
        <div className="hidden items-center gap-2 rounded-full border border-steel-200 bg-steel-50 px-3 py-1.5 sm:flex">
          <SecurityIcon />
          <span className="text-xs font-medium text-steel-600">
            {isConfigured ? 'Credentials set' : 'No credentials'}
          </span>
        </div>
      </div>
    </header>
  )
}

function ConnectionBadge({ status }) {
  const config = {
    checking: { label: 'Checking…', dot: 'bg-steel-400', text: 'text-steel-500' },
    online: { label: 'Backend connected', dot: 'bg-signal-low', text: 'text-signal-low' },
    offline: { label: 'Backend unavailable', dot: 'bg-signal-high', text: 'text-signal-high' },
    unauthorized: { label: 'Authentication required', dot: 'bg-signal-review', text: 'text-signal-review' }
  }[status] || { label: 'Unknown', dot: 'bg-steel-400', text: 'text-steel-500' }

  return (
    <div className="flex items-center gap-2 rounded-full border border-steel-200 bg-steel-50 px-3 py-1.5">
      <span className={`h-2 w-2 rounded-full ${config.dot}`} aria-hidden="true" />
      <span className={`text-xs font-medium ${config.text}`}>{config.label}</span>
    </div>
  )
}

function SecurityIcon() {
  return (
    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" strokeWidth="1.8" className="text-steel-500">
      <path d="M12 3.5l7 3v5c0 4.5-3 7.5-7 8.5-4-1-7-4-7-8.5v-5l7-3z" strokeLinejoin="round" />
    </svg>
  )
}
