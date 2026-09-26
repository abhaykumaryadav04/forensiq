// Exact verdict strings the backend's CentralRiskEngine can return.
// The frontend never invents or recalculates a verdict — it only
// ever renders what finalVerdict.verdict contains, falling back to
// a neutral "unrecognized" style for anything not listed here.
export const VERDICT_CONFIG = {
  VERIFIED: {
    label: 'Verified',
    tone: 'verified',
    description: 'The document passed all available checks with high confidence.'
  },
  LOW_RISK: {
    label: 'Low Risk',
    tone: 'low',
    description: 'No significant issues were found during screening.'
  },
  REVIEW_REQUIRED: {
    label: 'Review Required',
    tone: 'review',
    description: 'Some checks could not fully confirm this document. Manual review is recommended.'
  },
  SUSPICIOUS: {
    label: 'Suspicious',
    tone: 'suspicious',
    description: 'One or more checks flagged anomalies that warrant investigation.'
  },
  HIGH_RISK: {
    label: 'High Risk',
    tone: 'high',
    description: 'Multiple checks indicate a significant likelihood of manipulation or fraud.'
  },
  UNSUPPORTED_DOCUMENT: {
    label: 'Unsupported Document',
    tone: 'unknown',
    description: 'This document type is not currently supported for screening.'
  },
  LOW_QUALITY: {
    label: 'Low Quality',
    tone: 'unknown',
    description: 'The captured image quality was insufficient for reliable screening.'
  },
  PROCESSING_ERROR: {
    label: 'Processing Error',
    tone: 'unknown',
    description: 'An error occurred while processing this document.'
  }
}

export function getVerdictConfig(verdict) {
  return (
    VERDICT_CONFIG[verdict] || {
      label: verdict ? verdict.replaceAll('_', ' ') : 'Unknown',
      tone: 'unknown',
      description: ''
    }
  )
}

export const TONE_STYLES = {
  verified: { bg: 'bg-signal-verified/10', text: 'text-signal-verified', border: 'border-signal-verified/30', dot: 'bg-signal-verified' },
  low: { bg: 'bg-signal-low/10', text: 'text-signal-low', border: 'border-signal-low/30', dot: 'bg-signal-low' },
  review: { bg: 'bg-signal-review/10', text: 'text-signal-review', border: 'border-signal-review/30', dot: 'bg-signal-review' },
  suspicious: { bg: 'bg-signal-suspicious/10', text: 'text-signal-suspicious', border: 'border-signal-suspicious/30', dot: 'bg-signal-suspicious' },
  high: { bg: 'bg-signal-high/10', text: 'text-signal-high', border: 'border-signal-high/30', dot: 'bg-signal-high' },
  unknown: { bg: 'bg-signal-unknown/10', text: 'text-signal-unknown', border: 'border-signal-unknown/30', dot: 'bg-signal-unknown' }
}
