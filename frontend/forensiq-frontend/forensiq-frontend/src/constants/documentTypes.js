// Mirrors backend DocumentType enum: UNKOWN, PASSPORT, DRIVING_LICENSE, ID_CARD, GOVERNMENT_ID
export const DOCUMENT_TYPE_LABELS = {
  UNKOWN: 'Unknown',
  PASSPORT: 'Passport',
  DRIVING_LICENSE: 'Driving License',
  ID_CARD: 'ID Card',
  GOVERNMENT_ID: 'Government ID'
}

export function documentTypeLabel(type) {
  if (!type) return 'Unknown'
  return DOCUMENT_TYPE_LABELS[type] || type.replaceAll('_', ' ')
}
