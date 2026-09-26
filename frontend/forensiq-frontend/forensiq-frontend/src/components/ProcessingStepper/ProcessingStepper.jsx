import { PROCESSING_STEPS } from '../../constants/processingSteps.js'

/**
 * Since the backend returns one response at the end of the pipeline
 * rather than incremental progress events, `activeIndex` is driven by
 * elapsed time (see useSimulatedProgress) purely to give the person a
 * sense of motion — it never claims a percentage the backend didn't
 * report. `outcome` ('completed' | 'failed' | null) resolves every
 * step at once when the real response arrives.
 */
export default function ProcessingStepper({ activeIndex, outcome }) {
  return (
    <ol className="space-y-1">
      {PROCESSING_STEPS.map((step, index) => {
        let state = 'waiting'
        if (outcome === 'completed') state = 'completed'
        else if (outcome === 'failed') state = index <= activeIndex ? 'failed' : 'waiting'
        else if (index < activeIndex) state = 'completed'
        else if (index === activeIndex) state = 'processing'

        return (
          <li key={step.key} className="flex items-center gap-3 rounded-md px-2 py-2">
            <StepIcon state={state} />
            <span
              className={`text-sm ${
                state === 'waiting' ? 'text-steel-400' : state === 'failed' ? 'text-signal-high font-medium' : 'text-ink-800'
              }`}
            >
              {step.label}
            </span>
          </li>
        )
      })}
    </ol>
  )
}

function StepIcon({ state }) {
  if (state === 'completed') {
    return (
      <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-signal-low text-white">
        <svg viewBox="0 0 16 16" width="10" height="10" fill="none" stroke="currentColor" strokeWidth="2.4">
          <path d="M3 8l3.2 3.2L13 4.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </span>
    )
  }
  if (state === 'processing') {
    return (
      <span className="flex h-5 w-5 shrink-0 items-center justify-center">
        <span className="h-3 w-3 animate-pulse rounded-full bg-accent" />
      </span>
    )
  }
  if (state === 'failed') {
    return (
      <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-signal-high text-white">
        <svg viewBox="0 0 16 16" width="10" height="10" fill="none" stroke="currentColor" strokeWidth="2.4">
          <path d="M4 4l8 8M12 4l-8 8" strokeLinecap="round" />
        </svg>
      </span>
    )
  }
  return <span className="h-5 w-5 shrink-0 rounded-full border-2 border-steel-200" />
}
