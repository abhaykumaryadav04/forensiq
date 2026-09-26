import { useMemo, useState } from 'react'
import Card from '../components/common/Card.jsx'
import EmptyState from '../components/common/EmptyState.jsx'
import { useScanHistory } from '../context/ScanHistoryContext.jsx'
import { getVerdictConfig, TONE_STYLES, VERDICT_CONFIG } from '../constants/verdicts.js'
import { DOCUMENT_TYPE_LABELS, documentTypeLabel } from '../constants/documentTypes.js'
import ReportButton from '../components/Report/ReportButton.jsx'

export default function ScanHistory() {
  const { scans } = useScanHistory()
  const [query, setQuery] = useState('')
  const [verdictFilter, setVerdictFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')

  const filtered = useMemo(() => {
    return scans.filter((scan) => {
      if (verdictFilter && scan.verdict !== verdictFilter) return false
      if (typeFilter && scan.documentType !== typeFilter) return false
      if (query) {
        const q = query.toLowerCase()
        const haystack = `${scan.documentId} ${scan.requestId} ${scan.documentType}`.toLowerCase()
        if (!haystack.includes(q)) return false
      }
      return true
    })
  }, [scans, query, verdictFilter, typeFilter])

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">Scan History</h2>
        <p className="text-sm text-steel-500">
          The backend doesn't yet expose a history-listing endpoint, so this shows real scan results from this browser
          session only.
        </p>
      </div>

      <Card>
        <div className="flex flex-wrap gap-3">
          <input
            type="search"
            placeholder="Search by document ID or request ID"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="focus-ring flex-1 min-w-[200px] rounded-md border border-steel-300 px-3 py-2 text-sm"
          />
          <select
            value={verdictFilter}
            onChange={(e) => setVerdictFilter(e.target.value)}
            className="focus-ring rounded-md border border-steel-300 px-3 py-2 text-sm"
          >
            <option value="">All verdicts</option>
            {Object.keys(VERDICT_CONFIG).map((v) => (
              <option key={v} value={v}>
                {VERDICT_CONFIG[v].label}
              </option>
            ))}
          </select>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="focus-ring rounded-md border border-steel-300 px-3 py-2 text-sm"
          >
            <option value="">All document types</option>
            {Object.keys(DOCUMENT_TYPE_LABELS).map((t) => (
              <option key={t} value={t}>
                {DOCUMENT_TYPE_LABELS[t]}
              </option>
            ))}
          </select>
        </div>
      </Card>

      <Card bodyClassName="p-0">
        {filtered.length === 0 ? (
          <div className="p-5">
            <EmptyState title="No matching scans" description="Try adjusting your search or filters, or run a new scan." />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-steel-50 text-left text-xs font-semibold uppercase tracking-wide text-steel-500">
                  <th className="px-4 py-2.5">Document ID</th>
                  <th className="px-4 py-2.5">Type</th>
                  <th className="px-4 py-2.5">Verdict</th>
                  <th className="px-4 py-2.5">Risk Score</th>
                  <th className="px-4 py-2.5">Date</th>
                  <th className="px-4 py-2.5">Processing Time</th>
                  <th className="px-4 py-2.5">Status</th>
                  <th className="px-4 py-2.5" />
                </tr>
              </thead>
              <tbody className="divide-y divide-steel-100">
                {filtered.map((scan, i) => {
                  const config = getVerdictConfig(scan.verdict)
                  const tone = TONE_STYLES[config.tone]
                  return (
                    <tr key={i} className="odd:bg-white even:bg-steel-50/50">
                      <td className="px-4 py-2.5 font-medium text-ink-900">#{scan.documentId}</td>
                      <td className="px-4 py-2.5 text-ink-700">{documentTypeLabel(scan.documentType)}</td>
                      <td className="px-4 py-2.5">
                        <span className={`inline-flex items-center gap-1.5 rounded-full border px-2 py-0.5 text-xs font-medium ${tone.bg} ${tone.text} ${tone.border}`}>
                          {config.label}
                        </span>
                      </td>
                      <td className="px-4 py-2.5 font-mono text-ink-700">
                        {typeof scan.riskScore === 'number' ? scan.riskScore.toFixed(1) : '—'}
                      </td>
                      <td className="px-4 py-2.5 text-steel-500">{new Date(scan.recordedAt).toLocaleString()}</td>
                      <td className="px-4 py-2.5 text-steel-500">
                        {typeof scan.processingTimeTaken === 'number' ? `${(scan.processingTimeTaken / 1000).toFixed(2)}s` : '—'}
                      </td>
                      <td className="px-4 py-2.5 text-steel-500">{scan.status || '—'}</td>
                      <td className="px-4 py-2.5">
                        <ReportButton documentId={scan.documentId} className="px-3 py-1.5 text-xs" />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
