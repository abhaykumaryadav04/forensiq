import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: BASE_URL,
  timeout: 1200000
})

// Attaches HTTP Basic credentials (required by the backend's
// SecurityConfig on every route except /actuator/health) from
// whatever the AuthContext currently holds. See useApiAuthSync below.
let currentCredentials = null

export function setApiCredentials(credentials) {
  currentCredentials = credentials
}

api.interceptors.request.use((config) => {
  if (currentCredentials?.username) {
    config.auth = {
      username: currentCredentials.username,
      password: currentCredentials.password || ''
    }
  }
  return config
})

export function toApiErrorMessage(error) {
  if (!error?.response) {
    if (error?.code === 'ECONNABORTED') {
      return 'The request timed out. The ForensiQ backend may be under heavy load.'
    }
    return 'ForensiQ backend is unavailable. Please check the server and your network connection.'
  }
  const { status, data } = error.response
  const backendMessage = typeof data === 'string' ? data : data?.message
  switch (status) {
    case 400:
      return backendMessage || 'The request was malformed. Please try again.'
    case 401:
      return 'Authentication failed. Check the backend username and password in Settings.'
    case 403:
      return 'You are not authorized to perform this action.'
    case 404:
      return backendMessage || 'The requested resource was not found.'
    case 422:
      return backendMessage || 'The document could not be processed. Please check the capture and try again.'
    case 500:
      return backendMessage || 'ForensiQ encountered an internal error while processing the document.'
    default:
      return backendMessage || `Unexpected error (HTTP ${status}).`
  }
}

export default api
