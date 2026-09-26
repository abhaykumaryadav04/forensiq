import api from './api.js'

/**
 * Downloads the PDF screening report for a document and triggers a
 * browser save-as. GET /api/v1/reports/{documentId}
 */
export async function downloadReport(documentId) {
  const response = await api.get(`/api/v1/reports/${documentId}`, {
    responseType: 'blob'
  })
  const blob = new Blob([response.data], { type: 'application/pdf' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `ForensiQ-Report-${documentId}.pdf`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
