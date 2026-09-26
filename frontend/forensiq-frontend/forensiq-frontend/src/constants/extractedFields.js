// Mirrors backend ExtractedFieldType enum. Only fields the backend
// actually returns in extractedInformation.fields are ever rendered —
// this map is display labels only, not a schema the frontend invents.
export const FIELD_LABELS = {
  FULL_NAME: 'Full Name',
  FIRST_NAME: 'Given Name',
  LAST_NAME: 'Surname',
  DATE_OF_BIRTH: 'Date of Birth',
  NATIONALITY: 'Nationality',
  DOCUMENT_NUMBER: 'Document Number',
  PASSPORT_NUMBER: 'Passport Number',
  LICENSE_NUMBER: 'License Number',
  ID_NUMBER: 'Identity Number',
  AADHAAR_NUMBER: 'Aadhaar Number',
  EXPIRY_DATE: 'Date of Expiry',
  ISSUE_DATE: 'Date of Issue',
  GENDER: 'Sex',
  ADDRESS: 'Address',
  PLACE_OF_BIRTH: 'Place of Birth',
  ISSUING_COUNTRY: 'Issuing Country',
  ISSUING_AUTHORITY: 'Issuing Authority',
  NAME: 'Name',
  OTHER: 'Other'
}

export function fieldLabel(type) {
  if (!type) return 'Field'
  return FIELD_LABELS[type] || type.replaceAll('_', ' ')
}
