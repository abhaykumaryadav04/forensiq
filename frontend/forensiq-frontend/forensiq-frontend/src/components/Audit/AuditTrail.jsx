import EmptyState from '../common/EmptyState.jsx'

// Renders the session-logged scans (see ScanHistoryContext) as a hash
// chain: each entry links to the previous entry's audit hash, the way
// ScreeningAuditService computes auditHash / previousAuditHash server
// side. We only show entries this browser actually observed via real
// API responses — there is no GET endpoint yet to fetch the backend's
// full audit ledger.
export default function AuditTrail({ entries }) {
  if (!entries || entries.length === 0) {
    return (
      <EmptyState
        title="No audit entries yet"
        description="Complete a scan to see its audit record here. This log reflects only what this browser session has observed — the backend does not yet expose a full audit-history endpoint."
      />
    )
  }

  return (
    <ol className="space-y-3">
      {entries.map((entry, i) => (
        <li key={i} className="rounded-lg border border-steel-200 bg-white p-4 shadow-card">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="text-sm font-semibold text-ink-900">Document #{entry.documentId}</span>
            <span className="text-xs text-steel-500">{new Date(entry.recordedAt).toLocaleString()}</span>
          </div>
          <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-2 text-xs sm:grid-cols-3">
            <Field label="Request ID" value={entry.requestId} mono />
            <Field label="Document Hash" value={entry.documentHash} mono truncate />
            <Field label="Verdict" value={entry.verdict} />
            <Field label="Risk Score" value={entry.riskScore != null ? entry.riskScore.toFixed(1) : '—'} />
            <Field label="Decision Confidence" value={entry.decisionConfidence} />
            <Field label="Checks Performed" value={entry.checksPerformed} />
          </dl>
          {i < entries.length - 1 && (
            <div className="mt-3 flex items-center gap-2 border-t border-dashed border-steel-200 pt-2 text-[11px] text-steel-400">
              <ChainIcon />
              chained after document #{entries[i + 1].documentId}
            </div>
          )}
        </li>
      ))}
    </ol>
  )
}

function Field({ label, value, mono, truncate }) {
  return (
    <div>
      <dt className="text-steel-500">{label}</dt>
      <dd className={`mt-0.5 text-ink-800 ${mono ? 'font-mono' : ''} ${truncate ? 'truncate' : ''}`}>{value ?? '—'}</dd>
    </div>
  )
}

function ChainIcon() {
  return (
    <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M9 15l6-6M10 6l1.5-1.5a3 3 0 114.2 4.2L14 10M14 18l-1.5 1.5a3 3 0 11-4.2-4.2L10 14" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
