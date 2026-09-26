import { useState } from 'react'
import Card from '../components/common/Card.jsx'
import EmptyState from '../components/common/EmptyState.jsx'
import ReportButton from '../components/Report/ReportButton.jsx'
import { useScanHistory } from '../context/ScanHistoryContext.jsx'
import { documentTypeLabel } from '../constants/documentTypes.js'

export default function Reports() {
  const { scans } = useScanHistory()
  const [manualId, setManualId] = useState('')

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">Reports</h2>
        <p className="text-sm text-steel-500">Download the PDF screening report for any completed document.</p>
      </div>

      <Card title="Download by Document ID">
        <form
          className="flex flex-wrap items-end gap-3"
          onSubmit={(e) => e.preventDefault()}
        >
          <div>
            <label htmlFor="doc-id" className="mb-1 block text-xs font-medium text-steel-500">
              Document ID
            </label>
            <input
              id="doc-id"
              type="text"
              value={manualId}
              onChange={(e) => setManualId(e.target.value)}
              placeholder="e.g. 27"
              className="focus-ring w-48 rounded-md border border-steel-300 px-3 py-2 text-sm"
            />
          </div>
          <ReportButton documentId={manualId || null} />
        </form>
      </Card>

      <Card title="This Session's Scans">
        {scans.length === 0 ? (
          <EmptyState title="No scans yet" description="Reports for completed scans will appear here." />
        ) : (
          <div className="divide-y divide-steel-100">
            {scans.map((scan, i) => (
              <div key={i} className="flex flex-wrap items-center justify-between gap-3 py-3">
                <div>
                  <div className="text-sm font-medium text-ink-900">
                    Document #{scan.documentId} · {documentTypeLabel(scan.documentType)}
                  </div>
                  <div className="text-xs text-steel-500">{new Date(scan.recordedAt).toLocaleString()}</div>
                </div>
                <ReportButton documentId={scan.documentId} className="px-3 py-1.5 text-xs" />
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  )
}
