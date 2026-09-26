import { useEffect, useRef, useState } from 'react'
import { PROCESSING_STEPS } from '../constants/processingSteps.js'

const STEP_INTERVAL_MS = 900

// Advances a step index on a timer while `active` is true, purely for
// visual feedback during the single long-running POST the backend
// performs. Never overtakes the second-to-last step, since we don't
// know real completion until the response returns.
export function useSimulatedProgress(active) {
  const [activeIndex, setActiveIndex] = useState(0)
  const timerRef = useRef(null)

  useEffect(() => {
    if (!active) {
      setActiveIndex(0)
      if (timerRef.current) clearInterval(timerRef.current)
      return
    }
    timerRef.current = setInterval(() => {
      setActiveIndex((i) => Math.min(i + 1, PROCESSING_STEPS.length - 1))
    }, STEP_INTERVAL_MS)
    return () => clearInterval(timerRef.current)
  }, [active])

  return activeIndex
}
