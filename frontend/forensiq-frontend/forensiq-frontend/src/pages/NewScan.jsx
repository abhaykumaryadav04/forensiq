import { useCallback, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Card from '../components/common/Card.jsx'
import ErrorState from '../components/common/ErrorState.jsx'
import CameraScanner from '../components/CameraScanner/CameraScanner.jsx'
import ProcessingStepper from '../components/ProcessingStepper/ProcessingStepper.jsx'
import { useSimulatedProgress } from '../hooks/useSimulatedProgress.js'
import { createScreening, scanCameraImage } from '../services/screeningApi.js'
import { toApiErrorMessage } from '../services/api.js'
import { useScanHistory } from '../context/ScanHistoryContext.jsx'
import { PROCESSING_STEPS } from '../constants/processingSteps.js'

// idle -> preview (manual capture, awaiting confirmation) -> processing -> done/error
export default function NewScan() {
  const navigate = useNavigate()
  const { recordScan } = useScanHistory()
  const abortRef = useRef(null)

  const [phase, setPhase] = useState('idle')
  const [captured, setCaptured] = useState(null) // { blob, previewUrl, auto }
  const [error, setError] = useState(null)
  const [scanKey, setScanKey] = useState(0) // remounts CameraScanner on retake

  const activeIndex = useSimulatedProgress(phase === 'processing')

  const runScan = useCallback(
    async (blob) => {
      setPhase('processing')
      setError(null)
      const controller = new AbortController()
      abortRef.current = controller
      try {
        const request = await createScreening()
        const requestId = request.requestId
        const result = await scanCameraImage(requestId, blob, { signal: controller.signal })

        if (!result.accepted || !result.document?.id) {
          setError(result.message || 'The document could not be screened.')
          setPhase('error')
          return
        }

        recordScan({
          documentId: result.document.id,
          requestId,
          documentHash: result.document.fileHash,
          documentType: result.document.documentType,
          verdict: result.finalVerdict?.verdict,
          riskScore: result.finalVerdict?.riskScore,
          decisionConfidence: result.finalVerdict?.decisionConfidence,
          checksPerformed: result.finalVerdict?.checksPerformed,
          checksAvailable: result.finalVerdict?.checksAvailable,
          processingTimeTaken: result.finalVerdict?.processingTimeTaken,
          status: result.document.status
        })

        navigate(`/result/${result.document.id}`, { state: { response: result } })
      } catch (err) {
        if (err.code === 'ERR_CANCELED') return
        setError(toApiErrorMessage(err))
        setPhase('error')
      }
    },
    [navigate, recordScan]
  )

  const handleCapture = useCallback(
    (blob, previewUrl) => {
      if (!blob) return
      // Distinguish auto vs manual isn't passed explicitly by
      // CameraScanner today (it calls onCapture the same way for
      // both); we treat every capture as needing confirmation so the
      // person always gets a chance to retake before it's sent.
      setCaptured({ blob, previewUrl })
      setPhase('preview')
    },
    []
  )

  function retake() {
    if (captured?.previewUrl) URL.revokeObjectURL(captured.previewUrl)
    setCaptured(null)
    setError(null)
    setPhase('idle')
    setScanKey((k) => k + 1)
  }

  function confirmScan() {
    if (captured?.blob) runScan(captured.blob)
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-ink-900">New Scan</h2>
        <p className="text-sm text-steel-500">Capture an identity document with your camera, or upload a photo.</p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <div className="lg:col-span-3">
          <Card bodyClassName="p-0" className="overflow-hidden">
            {phase === 'idle' && (
              <div className="p-4">
                <CameraScanner key={scanKey} onCapture={handleCapture} />
              </div>
            )}
            {phase === 'preview' && captured && (
              <div className="p-4">
                <div className="overflow-hidden rounded-lg bg-ink-950">
                  <img src={captured.previewUrl} alt="Captured document" className="w-full object-contain" />
                </div>
                <div className="mt-4 flex flex-wrap gap-3">
                  <button
                    type="button"
                    onClick={retake}
                    className="focus-ring rounded-md border border-steel-300 bg-white px-4 py-2.5 text-sm font-medium text-ink-800 hover:bg-steel-50"
                  >
                    Retake
                  </button>
                  <button
                    type="button"
                    onClick={confirmScan}
                    className="focus-ring rounded-md bg-accent px-4 py-2.5 text-sm font-medium text-white hover:bg-accent-dim"
                  >
                    Scan Document
                  </button>
                </div>
              </div>
            )}
            {(phase === 'processing' || phase === 'error') && captured && (
              <div className="p-4">
                <div className="overflow-hidden rounded-lg bg-ink-950">
                  <img src={captured.previewUrl} alt="Captured document" className="w-full object-contain opacity-70" />
                </div>
              </div>
            )}
          </Card>
        </div>

        <div className="lg:col-span-2 space-y-4">
          <Card title="Instructions">
            <ul className="space-y-2 text-sm text-ink-700">
              <li>Place the document flat, well-lit, and fully inside the guide frame.</li>
              <li>Hold steady — the camera captures automatically once the document stops moving.</li>
              <li>Prefer a manual capture? Use the shutter button any time.</li>
              <li>No camera? Upload a photo instead.</li>
            </ul>
          </Card>

          {phase === 'processing' && (
            <Card title="Screening in progress" subtitle="This can take a few seconds">
              <ProcessingStepper activeIndex={activeIndex} outcome={null} />
            </Card>
          )}

          {phase === 'error' && (
            <ErrorState
              title="Screening failed"
              message={error}
              onRetry={() => {
                setPhase('preview')
                setError(null)
              }}
            />
          )}

          {phase === 'idle' && (
            <Card title="Processing steps" subtitle="Runs automatically once a document is captured">
              <ol className="space-y-1 text-sm text-steel-500">
                {PROCESSING_STEPS.map((step) => (
                  <li key={step.key} className="flex items-center gap-2">
                    <span className="h-1.5 w-1.5 rounded-full bg-steel-300" />
                    {step.label}
                  </li>
                ))}
              </ol>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}
