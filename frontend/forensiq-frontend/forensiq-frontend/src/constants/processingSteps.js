// Mirrors the pipeline the backend documents performing on
// POST /api/v1/screenigs/{requestId}/camera. The backend returns a
// single response at the end (no incremental progress events), so the
// frontend animates through these labels while the request is in
// flight and marks the last relevant step "failed" if the call errors.
export const PROCESSING_STEPS = [
  { key: 'validation', label: 'Image validation' },
  { key: 'quality', label: 'Image quality' },
  { key: 'detection', label: 'Document detection' },
  { key: 'ocr', label: 'OCR' },
  { key: 'classification', label: 'Document classification' },
  { key: 'extraction', label: 'Information extraction' },
  { key: 'mrz', label: 'MRZ / QR / Barcode' },
  { key: 'tampering', label: 'Tampering analysis' },
  { key: 'crossfield', label: 'Cross-field verification' },
  { key: 'risk', label: 'Risk assessment' }
]
