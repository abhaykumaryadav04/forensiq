import api from './api.js'

/**
 * Creates a new screening request.
 * POST /api/v1/screenigs
 * Returns the created ScreeningRequest: { id, requestId, status, createdAt, ... }
 */
export async function createScreening() {
  const response = await api.post('/api/v1/screenigs')
  return response.data
}

/**
 * Submits a captured/uploaded image for full document screening.
 * POST /api/v1/screenigs/{requestId}/camera  (multipart/form-data, field name "image")
 * Runs image validation, quality analysis, document detection, OCR,
 * classification, extraction, MRZ/QR verification, tampering analysis,
 * cross-field verification, risk assessment and returns the final verdict.
 */
export async function scanCameraImage(requestId, imageFile, { signal } = {}) {
  const formData = new FormData()
  formData.append('image', imageFile)
  const response = await api.post(`/api/v1/screenigs/${requestId}/camera`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    signal
  })
  return response.data
}

/**
 * Health probe used for the backend connection indicator.
 * GET /actuator/health — the only endpoint permitted without auth.
 */
export async function checkBackendHealth() {
  const response = await api.get('/actuator/health', { timeout: 5000 })
  return response.data
}
