import { fieldLabel } from '../../constants/extractedFields.js'
import EmptyState from '../common/EmptyState.jsx'

export default function ExtractedFields({ extraction }) {
  const fields = extraction?.fields || []

  if (fields.length === 0) {
    return (
      <EmptyState
        title="No extracted fields"
        description="The backend did not return any extracted information for this document."
      />
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-ink-900">Extracted Information</h3>
        {extraction?.confodence != null && (
          <span className="text-xs text-steel-500">
            OCR confidence: <span className="font-mono text-ink-700">{extraction.confodence.toFixed(1)}</span>
          </span>
        )}
      </div>
      <div className="mt-3 overflow-hidden rounded-lg border border-steel-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-steel-50 text-left text-xs font-semibold uppercase tracking-wide text-steel-500">
              <th className="px-4 py-2.5">Field</th>
              <th className="px-4 py-2.5">Value</th>
              <th className="px-4 py-2.5">Source</th>
              <th className="px-4 py-2.5">Confidence</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-steel-100">
            {fields.map((field, i) => (
              <tr key={i} className="odd:bg-white even:bg-steel-50/50">
                <td className="px-4 py-2.5 font-medium text-ink-900">{fieldLabel(field.extractedFieldType)}</td>
                <td className="px-4 py-2.5 text-ink-800">{field.value || '—'}</td>
                <td className="px-4 py-2.5 text-steel-500">{field.source || '—'}</td>
                <td className="px-4 py-2.5 font-mono text-steel-500">
                  {typeof field.confidencce === 'number' ? field.confidencce.toFixed(1) : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
