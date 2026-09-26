import { useCallback, useEffect, useRef, useState } from 'react'
import DocumentGuideFrame from './DocumentGuideFrame.jsx'
import CaptureStatus from './CaptureStatus.jsx'
import Spinner from '../common/Spinner.jsx'

const SAMPLE_INTERVAL_MS = 150
const STABLE_SAMPLES_REQUIRED = Math.ceil(1000 / SAMPLE_INTERVAL_MS) // ~1 second
const DIFF_THRESHOLD = 6 // average per-pixel luma delta considered "stable"
const SAMPLE_SIZE = 48 // small offscreen canvas keeps the diff cheap

/**
 * Live webcam capture with a lightweight browser-side stability check:
 * we sample a shrunk grayscale version of the guide-frame region and
 * compare consecutive frames. When the frame-to-frame difference stays
 * below a threshold for ~1 second we treat the document as "held
 * steady" and auto-capture. This is only a UX nicety — the backend
 * remains the sole authority on whether a document was actually
 * detected, since that response drives everything downstream.
 */
export default function CameraScanner({ onCapture, disabled }) {
  const videoRef = useRef(null)
  const sampleCanvasRef = useRef(document.createElement('canvas'))
  const captureCanvasRef = useRef(document.createElement('canvas'))
  const streamRef = useRef(null)
  const prevFrameRef = useRef(null)
  const stableCountRef = useRef(0)
  const intervalRef = useRef(null)
  const capturedRef = useRef(false)

  const [permissionState, setPermissionState] = useState('requesting') // requesting | granted | denied | unavailable
  const [captureState, setCaptureState] = useState('idle') // idle | positioning | detected | capturing
  const [autoCaptureEnabled, setAutoCaptureEnabled] = useState(true)

  const stopStream = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current)
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
  }, [])

  const capture = useCallback(() => {
    if (capturedRef.current || disabled) return
    const video = videoRef.current
    if (!video || video.videoWidth === 0) return
    capturedRef.current = true
    setCaptureState('capturing')

    const canvas = captureCanvasRef.current
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

    canvas.toBlob(
      (blob) => {
        if (blob) onCapture(blob, canvas.toDataURL('image/jpeg', 0.92))
        stopStream()
      },
      'image/jpeg',
      0.92
    )
  }, [onCapture, disabled, stopStream])

  const sampleStability = useCallback(() => {
    const video = videoRef.current
    if (!video || video.videoWidth === 0 || capturedRef.current) return

    const canvas = sampleCanvasRef.current
    canvas.width = SAMPLE_SIZE
    canvas.height = SAMPLE_SIZE
    const ctx = canvas.getContext('2d', { willReadFrequently: true })
    ctx.drawImage(video, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
    const frame = ctx.getImageData(0, 0, SAMPLE_SIZE, SAMPLE_SIZE).data

    const luma = new Float32Array(SAMPLE_SIZE * SAMPLE_SIZE)
    for (let i = 0; i < luma.length; i++) {
      const o = i * 4
      luma[i] = 0.299 * frame[o] + 0.587 * frame[o + 1] + 0.114 * frame[o + 2]
    }

    const prev = prevFrameRef.current
    if (prev) {
      let total = 0
      for (let i = 0; i < luma.length; i++) total += Math.abs(luma[i] - prev[i])
      const avgDiff = total / luma.length

      if (avgDiff < DIFF_THRESHOLD) {
        stableCountRef.current += 1
        setCaptureState(stableCountRef.current >= 2 ? 'detected' : 'positioning')
        if (autoCaptureEnabled && stableCountRef.current >= STABLE_SAMPLES_REQUIRED) {
          capture()
        }
      } else {
        stableCountRef.current = 0
        setCaptureState('positioning')
      }
    }
    prevFrameRef.current = luma
  }, [autoCaptureEnabled, capture])

  useEffect(() => {
    let cancelled = false
    async function start() {
      if (!navigator.mediaDevices?.getUserMedia) {
        setPermissionState('unavailable')
        return
      }
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment', width: { ideal: 1920 }, height: { ideal: 1080 } },
          audio: false
        })
        if (cancelled) {
          stream.getTracks().forEach((t) => t.stop())
          return
        }
        streamRef.current = stream
        if (videoRef.current) {
          videoRef.current.srcObject = stream
          await videoRef.current.play()
        }
        setPermissionState('granted')
        setCaptureState('positioning')
        intervalRef.current = setInterval(sampleStability, SAMPLE_INTERVAL_MS)
      } catch (error) {
        setPermissionState(error?.name === 'NotFoundError' ? 'unavailable' : 'denied')
      }
    }
    start()
    return () => {
      cancelled = true
      stopStream()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (permissionState === 'denied') {
    return (
      <ScannerMessage
        title="Camera access needed"
        message="Camera permission is required to capture the document. Please allow camera access in your browser settings and reload the page."
      />
    )
  }
  if (permissionState === 'unavailable') {
    return (
      <ScannerMessage
        title="Camera unavailable"
        message="No camera could be found on this device. You can still upload a photo of the document instead."
        showUpload
        onCapture={onCapture}
      />
    )
  }

  return (
    <div className="relative overflow-hidden rounded-lg bg-ink-950" style={{ aspectRatio: '4 / 3' }}>
      {permissionState === 'requesting' && (
        <div className="absolute inset-0 flex items-center justify-center">
          <Spinner className="h-8 w-8 text-white" />
        </div>
      )}
      <video ref={videoRef} className="h-full w-full object-cover" muted playsInline autoPlay />
      {permissionState === 'granted' && (
        <>
          <DocumentGuideFrame state={captureState} />
          <CaptureStatus state={captureState} />
        </>
      )}
      <div className="absolute right-3 top-3 flex items-center gap-2">
        <label className="flex items-center gap-1.5 rounded-full bg-ink-950/70 px-3 py-1.5 text-xs font-medium text-white backdrop-blur">
          <input
            type="checkbox"
            checked={autoCaptureEnabled}
            onChange={(e) => setAutoCaptureEnabled(e.target.checked)}
            className="h-3.5 w-3.5 accent-accent"
          />
          Auto-capture
        </label>
      </div>
      <div className="absolute bottom-5 right-4 flex gap-2">
        <button
          type="button"
          onClick={capture}
          disabled={permissionState !== 'granted' || disabled}
          className="focus-ring rounded-full bg-white p-3.5 text-ink-900 shadow-lg transition hover:bg-steel-100 disabled:opacity-50"
          aria-label="Capture photo manually"
        >
          <CameraIcon />
        </button>
      </div>
    </div>
  )
}

function ScannerMessage({ title, message, showUpload, onCapture }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-steel-200 bg-steel-50 px-6 py-16 text-center">
      <WarningIcon />
      <h3 className="text-sm font-semibold text-ink-900">{title}</h3>
      <p className="max-w-sm text-sm text-steel-500">{message}</p>
      {showUpload && (
        <label className="focus-ring mt-2 cursor-pointer rounded-md bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accent-dim">
          Upload photo instead
          <input
            type="file"
            accept="image/*"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0]
              if (file) onCapture(file, URL.createObjectURL(file))
            }}
          />
        </label>
      )}
    </div>
  )
}

function CameraIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.8">
      <path d="M4 8.5A1.5 1.5 0 015.5 7h2l1-2h7l1 2h2A1.5 1.5 0 0120 8.5v9A1.5 1.5 0 0118.5 19h-13A1.5 1.5 0 014 17.5v-9z" strokeLinejoin="round" />
      <circle cx="12" cy="13" r="3.2" />
    </svg>
  )
}
function WarningIcon() {
  return (
    <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" strokeWidth="1.6" className="text-signal-review">
      <path d="M12 4.5l9 15.5H3l9-15.5z" strokeLinejoin="round" />
      <path d="M12 10v4.5M12 17.2v.1" strokeLinecap="round" />
    </svg>
  )
}
