import { useState } from 'react'
import { downloadReport } from '../../services/reportApi.js'
import { toApiErrorMessage } from '../../services/api.js'
import Spinner from '../common/Spinner.jsx'

export default function ReportButton({ documentId, className = '' }) {
  const [state, setState] = useState('idle') // idle | downloading | error
  const [error, setError] = useState(null)

  async function handleClick() {
    setState('downloading')
    setError(null)
    try {
      await downloadReport(documentId)
      setState('idle')
    } catch (err) {
      setError(toApiErrorMessage(err))
      setState('error')
    }
  }

  return (
    <div>
      <button
        type="button"
        onClick={handleClick}
        disabled={state === 'downloading' || !documentId}
        className={`focus-ring inline-flex items-center gap-2 rounded-md bg-ink-900 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-ink-800 disabled:opacity-60 ${className}`}
      >
        {state === 'downloading' ? <Spinner className="h-4 w-4 text-white" /> : <DownloadIcon />}
        Download PDF Report
      </button>
      {state === 'error' && <p className="mt-1.5 text-xs text-signal-high">{error}</p>}
    </div>
  )
}

function DownloadIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="1.8">
      <path d="M12 4v11m0 0l-4-4m4 4l4-4" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M5 18.5h14" strokeLinecap="round" />
    </svg>
  )
}
