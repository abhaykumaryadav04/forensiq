import RiskScore from './RiskScore.jsx'
import { getVerdictConfig, TONE_STYLES } from '../../constants/verdicts.js'
import { documentTypeLabel } from '../../constants/documentTypes.js'

export default function VerdictCard({ verdict }) {
  if (!verdict) return null
  const config = getVerdictConfig(verdict.verdict)
  const tone = TONE_STYLES[config.tone]

  return (
    <div className={`rounded-xl border ${tone.border} bg-white shadow-card`}>
      <div className={`flex flex-col gap-4 rounded-t-xl border-b ${tone.border} ${tone.bg} px-6 py-5 sm:flex-row sm:items-center sm:justify-between`}>
        <div>
          <span className="inline-flex items-center gap-1.5 rounded-full bg-white/70 px-2.5 py-0.5 text-xs font-medium text-ink-700">
            {documentTypeLabel(verdict.documentType)}
          </span>
          <h2 className={`mt-2 text-2xl font-semibold ${tone.text}`}>{config.label}</h2>
          {config.description && <p className="mt-1 max-w-md text-sm text-ink-700">{config.description}</p>}
        </div>
        <span className={`h-3 w-3 shrink-0 rounded-full ${tone.dot}`} aria-hidden="true" />
      </div>

      <div className="grid grid-cols-1 gap-6 px-6 py-5 sm:grid-cols-2">
        <RiskScore score={verdict.riskScore} />
        <div className="grid grid-cols-2 gap-4 text-sm">
          <Stat label="Decision Confidence" value={verdict.decisionConfidence || 'Not available'} />
          <Stat
            label="Processing Time"
            value={
              typeof verdict.processingTimeTaken === 'number'
                ? `${(verdict.processingTimeTaken / 1000).toFixed(2)}s`
                : 'Not available'
            }
          />
          <Stat
            label="Checks Performed"
            value={
              verdict.checksPerformed != null && verdict.checksAvailable != null
                ? `${verdict.checksPerformed} / ${verdict.checksAvailable}`
                : 'Not available'
            }
          />
          <Stat label="Cached Result" value={verdict.cachedResult ? 'Yes' : 'No'} />
        </div>
      </div>

      {verdict.cachedResult && (
        <div className="mx-6 mb-5 flex items-center gap-2 rounded-md border border-accent/30 bg-accent/5 px-3 py-2 text-xs text-accent-dim">
          <span className="rounded-full bg-accent px-2 py-0.5 font-medium text-white">Previously screened</span>
          <span>This result was retrieved from the existing screening record.</span>
        </div>
      )}

      {(verdict.reasons?.length > 0 || verdict.warnings?.length > 0) && (
        <div className="grid grid-cols-1 gap-4 border-t border-steel-100 px-6 py-5 sm:grid-cols-2">
          {verdict.reasons?.length > 0 && (
            <ReasonList title="Reasons" items={verdict.reasons} tone="high" />
          )}
          {verdict.warnings?.length > 0 && (
            <ReasonList title="Warnings" items={verdict.warnings} tone="review" />
          )}
        </div>
      )}
    </div>
  )
}

function Stat({ label, value }) {
  return (
    <div>
      <div className="text-xs font-medium uppercase tracking-wide text-steel-500">{label}</div>
      <div className="mt-0.5 text-sm font-medium text-ink-900">{value}</div>
    </div>
  )
}

function ReasonList({ title, items, tone }) {
  const dotColor = tone === 'high' ? 'bg-signal-high' : 'bg-signal-review'
  return (
    <div>
      <h4 className="text-xs font-semibold uppercase tracking-wide text-steel-500">{title}</h4>
      <ul className="mt-2 space-y-1.5">
        {items.map((item, i) => (
          <li key={i} className="flex items-start gap-2 text-sm text-ink-800">
            <span className={`mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full ${dotColor}`} />
            {item}
          </li>
        ))}
      </ul>
    </div>
  )
}
