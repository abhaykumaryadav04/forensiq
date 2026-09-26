import EmptyState from '../common/EmptyState.jsx'

export default function TamperingAnalysis({ tampering }) {
  if (!tampering) {
    return <EmptyState title="No tampering analysis" description="The backend did not return a tampering analysis for this document." />
  }

  const { suspicious, tamperingScore, aiAnalysis, fieldTamperingRisks, indicators, issues } = tampering
  const flaggedFields = (fieldTamperingRisks || []).filter((r) => r.riskLevel && r.riskLevel !== 'LOW')

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Stat label="Suspicious" value={suspicious ? 'Yes' : 'No'} emphasize={suspicious} />
        <Stat label="Tampering Score" value={typeof tamperingScore === 'number' ? tamperingScore.toFixed(1) : '—'} />
        <Stat label="Attack Type" value={aiAnalysis?.attackType || '—'} />
        <Stat
          label="Attack Confidence"
          value={typeof aiAnalysis?.attackConfidence === 'number' ? aiAnalysis.attackConfidence.toFixed(1) : '—'}
        />
      </div>

      {suspicious && (
        <div className="rounded-md border border-signal-review/30 bg-signal-review/5 px-4 py-3 text-sm text-ink-800">
          Potential manipulation detected. {aiAnalysis?.reasoning || 'Review the flagged fields and forensic indicators below before making a decision.'}
        </div>
      )}

      {flaggedFields.length > 0 && (
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wide text-steel-500">Suspicious Fields</h4>
          <div className="mt-2 flex flex-wrap gap-2">
            {flaggedFields.map((r, i) => (
              <span
                key={i}
                className="rounded-full border border-signal-high/30 bg-signal-high/10 px-2.5 py-1 text-xs font-medium text-signal-high"
              >
                {r.affectedField} · {r.riskLevel}
              </span>
            ))}
          </div>
        </div>
      )}

      {(aiAnalysis?.observations?.length > 0 || issues?.length > 0) && (
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wide text-steel-500">Observations</h4>
          <ul className="mt-2 space-y-1.5">
            {[...(aiAnalysis?.observations || []), ...(issues || [])].map((obs, i) => (
              <li key={i} className="flex items-start gap-2 text-sm text-ink-800">
                <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-steel-400" />
                {obs}
              </li>
            ))}
          </ul>
        </div>
      )}

      {indicators?.length > 0 && (
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wide text-steel-500">Reasoning</h4>
          <div className="mt-2 space-y-2">
            {indicators.map((ind, i) => (
              <div key={i} className="flex items-center justify-between rounded-md border border-steel-200 px-3 py-2 text-sm">
                <div>
                  <span className="font-medium text-ink-900">{ind.analyzerName}</span>
                  {ind.description && <span className="ml-2 text-steel-500">{ind.description}</span>}
                </div>
                <span className={`text-xs font-mono ${ind.suspicious ? 'text-signal-high' : 'text-steel-400'}`}>
                  {ind.suspicionScore?.toFixed(1)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

function Stat({ label, value, emphasize }) {
  return (
    <div className="rounded-md border border-steel-200 px-3 py-2.5">
      <div className="text-[11px] font-medium uppercase tracking-wide text-steel-500">{label}</div>
      <div className={`mt-0.5 text-sm font-semibold ${emphasize ? 'text-signal-high' : 'text-ink-900'}`}>{value}</div>
    </div>
  )
}
