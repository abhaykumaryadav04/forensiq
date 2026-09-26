// componentRiskScores is defined on the backend's CentralRiskResult
// but FinalVerdictMapper does not currently copy it into
// FinalVerdictResponse, so this section renders nothing until that
// changes — we only ever display values the API actually sent.
export default function RiskBreakdown({ componentRiskScores }) {
  if (!componentRiskScores || Object.keys(componentRiskScores).length === 0) return null

  const entries = Object.entries(componentRiskScores)

  return (
    <div>
      <h3 className="text-sm font-semibold text-ink-900">Component Risk Breakdown</h3>
      <div className="mt-3 space-y-3">
        {entries.map(([key, value]) => (
          <div key={key}>
            <div className="flex items-center justify-between text-xs">
              <span className="font-medium text-ink-800">{key.replaceAll('_', ' ')}</span>
              <span className="font-mono text-steel-500">{value.toFixed(1)}</span>
            </div>
            <div className="mt-1 h-2 w-full overflow-hidden rounded-full bg-steel-100">
              <div
                className="h-full rounded-full bg-accent transition-all duration-500"
                style={{ width: `${Math.max(0, Math.min(100, value))}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
