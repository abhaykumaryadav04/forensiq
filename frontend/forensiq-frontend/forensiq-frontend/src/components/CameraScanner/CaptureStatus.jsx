const MESSAGES = {
  idle: 'Position document',
  positioning: 'Position document inside the frame',
  detected: 'Document detected — hold steady',
  capturing: 'Capturing…'
}

export default function CaptureStatus({ state = 'idle' }) {
  return (
    <div className="absolute bottom-5 left-1/2 w-max -translate-x-1/2 rounded-full bg-ink-950/80 px-4 py-2 text-sm font-medium text-white backdrop-blur">
      {MESSAGES[state] || MESSAGES.idle}
    </div>
  )
}
