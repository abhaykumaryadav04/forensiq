import { Link } from 'react-router-dom'
import Card from '../components/common/Card.jsx'
import EmptyState from '../components/common/EmptyState.jsx'
import StatusBadge from '../components/common/StatusBadge.jsx'
import useBackendStatus from '../hooks/useBackendStatus.js'
import { useScanHistory } from '../context/ScanHistoryContext.jsx'
import { getVerdictConfig, TONE_STYLES } from '../constants/verdicts.js'
import { documentTypeLabel } from '../constants/documentTypes.js'

const LOW_RISK_VERDICTS = new Set(['VERIFIED', 'LOW_RISK'])
const REVIEW_VERDICTS = new Set(['REVIEW_REQUIRED', 'LOW_QUALITY', 'UNSUPPORTED_DOCUMENT'])
const HIGH_RISK_VERDICTS = new Set(['SUSPICIOUS', 'HIGH_RISK'])

export default function Dashboard() {
  const { status } = useBackendStatus()
  const { scans } = useScanHistory()

  const total = scans.length
  const lowRisk = scans.filter((s) => LOW_RISK_VERDICTS.has(s.verdict)).length
  const review = scans.filter((s) => REVIEW_VERDICTS.has(s.verdict)).length
  const highRisk = scans.filter((s) => HIGH_RISK_VERDICTS.has(s.verdict)).length
  const avgTime =
    total > 0
      ? scans.reduce((sum, s) => sum + (s.processingTimeTaken || 0), 0) / scans.filter((s) => s.processingTimeTaken).length
      : null

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">Dashboard</h2>
        <p className="text-sm text-steel-500">
          Summary of scans performed in this browser session.
          {status === 'offline' && ' The ForensiQ backend is currently unreachable.'}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        <StatCard label="Total Scans" value={total} />
        <StatCard label="Verified / Low Risk" value={lowRisk} tone="low" />
        <StatCard label="Review Required" value={review} tone="review" />
        <StatCard label="Suspicious / High Risk" value={highRisk} tone="high" />
        <StatCard label="Avg. Processing Time" value={avgTime != null && !Number.isNaN(avgTime) ? `${(avgTime / 1000).toFixed(2)}s` : '—'} />
      </div>

      <Card
        title="Backend Connection"
        actions={<ConnectionPill status={status} />}
      >
        <p className="text-sm text-steel-600">
          {status === 'online' && 'The ForensiQ backend is reachable and responding to health checks.'}
          {status === 'offline' && 'ForensiQ backend is unavailable. Please check the server and your network connection.'}
          {status === 'unauthorized' && 'The backend is reachable but rejected the health check credentials. Check Settings.'}
          {status === 'checking' && 'Checking backend connectivity…'}
        </p>
      </Card>

      <Card title="Recent Scans" actions={<Link to="/history" className="text-sm font-medium text-accent hover:underline">View all</Link>}>
        {scans.length === 0 ? (
          <EmptyState
            title="No scans yet"
            description="Scans you perform will appear here."
            action={
              <Link to="/new-scan" className="focus-ring rounded-md bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-dim">
                Start a New Scan
              </Link>
            }
          />
        ) : (
          <div className="divide-y divide-steel-100">
            {scans.slice(0, 5).map((scan, i) => {
              const config = getVerdictConfig(scan.verdict)
              const tone = TONE_STYLES[config.tone]
              return (
                <div key={i} className="flex items-center justify-between py-3">
                  <div>
                    <div className="text-sm font-medium text-ink-900">
                      Document #{scan.documentId} · {documentTypeLabel(scan.documentType)}
                    </div>
                    <div className="text-xs text-steel-500">{new Date(scan.recordedAt).toLocaleString()}</div>
                  </div>
                  <span className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-medium ${tone.bg} ${tone.text} ${tone.border}`}>
                    {config.label}
                  </span>
                </div>
              )
            })}
          </div>
        )}
      </Card>
    </div>
  )
}

function StatCard({ label, value, tone }) {
  const toneClass = tone ? TONE_STYLES[tone].text : 'text-ink-900'
  return (
    <div className="rounded-lg border border-steel-200 bg-white p-4 shadow-card">
      <div className="text-xs font-medium uppercase tracking-wide text-steel-500">{label}</div>
      <div className={`mt-1 text-2xl font-semibold ${toneClass}`}>{value}</div>
    </div>
  )
}

function ConnectionPill({ status }) {
  const map = {
    online: 'PASSED',
    offline: 'FAILED',
    unauthorized: 'WARNING',
    checking: 'NOT_AVAILABLE'
  }
  const labels = {
    online: 'Connected',
    offline: 'Unavailable',
    unauthorized: 'Unauthorized',
    checking: 'Checking'
  }
  return <StatusBadge status={map[status]} label={labels[status]} />
}
