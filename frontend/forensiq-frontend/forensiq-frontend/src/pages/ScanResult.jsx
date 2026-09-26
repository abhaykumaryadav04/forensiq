import { useLocation, useNavigate, useParams, Link } from 'react-router-dom'
import Card from '../components/common/Card.jsx'
import EmptyState from '../components/common/EmptyState.jsx'
import VerdictCard from '../components/Verdict/VerdictCard.jsx'
import VerificationEvidence from '../components/Verification/VerificationEvidence.jsx'
import ExtractedFields from '../components/ExtractedFields/ExtractedFields.jsx'
import TamperingAnalysis from '../components/Tampering/TamperingAnalysis.jsx'
import RiskBreakdown from '../components/RiskBreakdown/RiskBreakdown.jsx'
import ReportButton from '../components/Report/ReportButton.jsx'
import { documentTypeLabel } from '../constants/documentTypes.js'

export default function ScanResult() {
  const { documentId } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()
  const response = state?.response

  if (!response) {
    return (
      <EmptyState
        title="Result not available"
        description="This result isn't in memory for this page (for example, after a refresh). The backend doesn't yet expose a way to re-fetch a past result by document ID — check Scan History for this session's results, or run a new scan."
        action={
          <div className="flex gap-3">
            <Link
              to="/history"
              className="focus-ring rounded-md border border-steel-300 bg-white px-4 py-2 text-sm font-medium text-ink-800 hover:bg-steel-50"
            >
              Scan History
            </Link>
            <Link to="/new-scan" className="focus-ring rounded-md bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-dim">
              New Scan
            </Link>
          </div>
        }
      />
    )
  }

  const { document, finalVerdict } = response

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-ink-900">Scan Result</h2>
          <p className="text-sm text-steel-500">
            {document?.fileName ? `${document.fileName} · ` : ''}
            {documentTypeLabel(document?.documentType)}
          </p>
        </div>
        <div className="flex gap-3">
          <ReportButton documentId={documentId} />
          <button
            type="button"
            onClick={() => navigate('/new-scan')}
            className="focus-ring rounded-md border border-steel-300 bg-white px-4 py-2.5 text-sm font-medium text-ink-800 hover:bg-steel-50"
          >
            New Scan
          </button>
        </div>
      </div>

      <VerdictCard verdict={finalVerdict} />

      {finalVerdict?.componentRiskScores && (
        <Card>
          <RiskBreakdown componentRiskScores={finalVerdict.componentRiskScores} />
        </Card>
      )}

      <Card>
        <VerificationEvidence
          extractedInformation={finalVerdict?.extractedInformation}
          verification={finalVerdict?.verification}
          tampering={finalVerdict?.tamperingAnalysis}
        />
      </Card>

      <Card title="Extracted Information">
        <ExtractedFields extraction={finalVerdict?.extractedInformation} />
      </Card>

      <Card title="Tampering Analysis">
        <TamperingAnalysis tampering={finalVerdict?.tamperingAnalysis} />
      </Card>
    </div>
  )
}
