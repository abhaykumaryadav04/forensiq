import Card from '../components/common/Card.jsx'
import AuditTrail from '../components/Audit/AuditTrail.jsx'
import { useScanHistory } from '../context/ScanHistoryContext.jsx'

export default function Audit() {
  const { scans } = useScanHistory()

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">Audit</h2>
        <p className="text-sm text-steel-500">
          Each screening the backend completes is recorded with a SHA-256 audit hash chained to the previous record.
          The backend doesn't yet expose an endpoint to list that ledger, so this shows the session's own scan
          responses as a local audit trail.
        </p>
      </div>

      <Card>
        <AuditTrail entries={scans} />
      </Card>
    </div>
  )
}
