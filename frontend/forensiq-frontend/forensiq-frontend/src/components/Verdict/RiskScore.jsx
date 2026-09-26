export default function RiskScore({ score }) {
  const value = typeof score === 'number' ? Math.max(0, Math.min(100, score)) : null
  const color = value == null ? '#5A7288' : value < 30 ? '#2E8B57' : value < 60 ? '#B8860B' : value < 80 ? '#C2410C' : '#B91C1C'

  return (
    <div>
      <div className="flex items-baseline justify-between">
        <span className="text-xs font-medium uppercase tracking-wide text-steel-500">Risk Score</span>
        <span className="font-mono text-sm text-ink-700">{value != null ? `${value.toFixed(1)} / 100` : 'Not available'}</span>
      </div>
      <div className="mt-2 h-2.5 w-full overflow-hidden rounded-full bg-steel-100">
        {value != null && (
          <div
            className="h-full rounded-full transition-all duration-500"
            style={{ width: `${value}%`, backgroundColor: color }}
          />
        )}
      </div>
      <div className="mt-1 flex justify-between text-[10px] text-steel-400">
        <span>0</span>
        <span>50</span>
        <span>100</span>
      </div>
    </div>
  )
}
