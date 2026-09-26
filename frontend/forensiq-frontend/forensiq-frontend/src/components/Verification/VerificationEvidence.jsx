import VerificationCard from './VerificationCard.jsx'

/**
 * Builds the "Verification Evidence" cards strictly from what the
 * backend returned in finalVerdict.verification and
 * finalVerdict.tamperingAnalysis (VarificationProcessingResult /
 * TamperingDetectionResult). No card is shown for data the backend
 * didn't provide — each getter below returns null when its source
 * object is missing, and the caller filters nulls out.
 */
export default function VerificationEvidence({ extractedInformation, verification, tampering }) {
  const cards = [
    ocrCard(extractedInformation),
    mrzCard(verification?.mrzValidatinResult),
    qrCard(verification?.qrvalidationResult),
    crossFieldCard(verification),
    classicalForensicsCard(tampering),
    aiAnalysisCard(tampering?.aiAnalysis),
    fieldTamperingCard(tampering)
  ].filter(Boolean)

  if (cards.length === 0) return null

  return (
    <div>
      <h3 className="text-sm font-semibold text-ink-900">Verification Evidence</h3>
      <p className="mt-1 text-xs text-steel-500">Every result below comes directly from the backend screening response.</p>
      <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card) => (
          <VerificationCard key={card.title} {...card} />
        ))}
      </div>
    </div>
  )
}

function ocrCard(extraction) {
  if (!extraction) return null
  const confidence = extraction.confodence
  return {
    title: 'OCR',
    status: confidence != null ? 'PASSED' : 'NOT_AVAILABLE',
    score: confidence != null ? confidence : null,
    explanation:
      extraction.fields?.length > 0
        ? `${extraction.fields.length} field${extraction.fields.length === 1 ? '' : 's'} extracted from the document text.`
        : 'No text fields were extracted from this document.'
  }
}

function mrzCard(mrz) {
  if (!mrz) return null
  const status = mrz.valid ? 'PASSED' : mrz.issues?.length ? 'FAILED' : 'NOT_AVAILABLE'
  return {
    title: 'MRZ',
    status,
    explanation: mrz.issues?.length ? mrz.issues.join(' ') : mrz.valid ? 'Machine-readable zone validated successfully.' : 'No MRZ issues reported.'
  }
}

function qrCard(qr) {
  if (!qr) return null
  let status = 'NOT_AVAILABLE'
  let explanation = 'No QR or barcode was available.'
  if (qr.detected) {
    if (qr.valid) {
      status = 'PASSED'
      explanation = 'QR/barcode decoded and validated successfully.'
    } else {
      status = 'FAILED'
      explanation = qr.issues?.length ? qr.issues.join(' ') : 'QR/barcode data could not be validated.'
    }
  } else if (qr.issues?.length) {
    status = 'WARNING'
    explanation = qr.issues.join(' ')
  }
  return { title: 'QR / Barcode', status, explanation }
}

function crossFieldCard(verification) {
  if (!verification || verification.crossfieldCheck == null) return null
  const issues = verification.crossFieldIssues
  const warnings = verification.crossFieldWarnings
  return {
    title: 'Cross-field Verification',
    status: verification.crossfieldCheck,
    score: verification.crossFieldRiskScore,
    explanation: issues?.length ? issues.join(' ') : warnings?.length ? warnings.join(' ') : 'Extracted fields agree across all available sources.'
  }
}

function classicalForensicsCard(tampering) {
  const sub = [tampering?.elaResult, tampering?.noiseResult, tampering?.copyMoveResult, tampering?.metadataResult].filter(Boolean)
  if (sub.length === 0) return null
  const anySuspicious = sub.some((s) => s.suspicious)
  const messages = sub.map((s) => s.message).filter(Boolean)
  return {
    title: 'Classical Forensics',
    status: anySuspicious ? 'WARNING' : 'PASSED',
    score: tampering.tamperingScore,
    explanation: anySuspicious
      ? messages.length
        ? messages.join(' ')
        : 'Moderate forensic anomaly detected.'
      : 'Error-level, noise, copy-move and metadata checks found no anomalies.'
  }
}

function aiAnalysisCard(ai) {
  if (!ai) return null
  return {
    title: 'AI / Visual Analysis',
    status: ai.suspicious ? 'SUSPICIOUS' : 'PASSED',
    score: ai.tamperingScore,
    explanation: ai.reasoning || (ai.observations?.length ? ai.observations.join(' ') : 'No visual anomalies observed.')
  }
}

function fieldTamperingCard(tampering) {
  if (!tampering || !Array.isArray(tampering.fieldTamperingRisks)) return null
  const risks = tampering.fieldTamperingRisks
  const flagged = risks.filter((r) => r.riskLevel && r.riskLevel !== 'LOW')
  return {
    title: 'Field Tampering',
    status: flagged.length > 0 ? 'WARNING' : risks.length > 0 ? 'PASSED' : 'NOT_AVAILABLE',
    explanation:
      flagged.length > 0
        ? `Potential manipulation detected in: ${flagged.map((r) => r.affectedField).join(', ')}.`
        : risks.length > 0
        ? 'No individual fields were flagged for tampering.'
        : 'No field-level tampering analysis was returned.'
  }
}
