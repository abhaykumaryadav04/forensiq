const STATUS_STYLES = {
  PASSED: 'bg-signal-low/10 text-signal-low border-signal-low/30',
  SUCCESS: 'bg-signal-low/10 text-signal-low border-signal-low/30',
  VALID: 'bg-signal-low/10 text-signal-low border-signal-low/30',
  WARNING: 'bg-signal-review/10 text-signal-review border-signal-review/30',
  FAILED: 'bg-signal-high/10 text-signal-high border-signal-high/30',
  NOT_AVAILABLE: 'bg-steel-100 text-steel-500 border-steel-200'
}

// Renders a small pill for a backend-provided status string (PASSED /
// FAILED / WARNING / NOT_AVAILABLE, etc). Never infers a status —
// always takes the exact value returned by the API.
export default function StatusBadge({ status, label }) {
  const style = STATUS_STYLES[status] || 'bg-steel-100 text-steel-600 border-steel-200'
  const text = label || (status ? status.replaceAll('_', ' ') : 'Unknown')
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-medium ${style}`}>
      {text}
    </span>
  )
}
