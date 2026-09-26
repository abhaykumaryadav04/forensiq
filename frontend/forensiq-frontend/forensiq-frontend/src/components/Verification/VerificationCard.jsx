import StatusBadge from '../common/StatusBadge.jsx'

export default function VerificationCard({ title, status, score, explanation, icon }) {
  return (
    <div className="rounded-lg border border-steel-200 bg-white p-4 shadow-card">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {icon}
          <h4 className="text-sm font-semibold text-ink-900">{title}</h4>
        </div>
        <StatusBadge status={status} />
      </div>
      {score != null && (
        <div className="mt-2 text-xs text-steel-500">
          Score: <span className="font-mono text-ink-700">{typeof score === 'number' ? score.toFixed(1) : score}</span>
        </div>
      )}
      {explanation && <p className="mt-2 text-sm text-ink-700">{explanation}</p>}
    </div>
  )
}
