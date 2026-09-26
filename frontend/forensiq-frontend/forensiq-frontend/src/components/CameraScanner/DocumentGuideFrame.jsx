const STATE_STYLES = {
  idle: 'border-white/70',
  positioning: 'border-signal-review',
  detected: 'border-signal-low',
  capturing: 'border-accent'
}

// Purely visual guide — the backend is what actually detects the
// document; this just tells the person where to hold it.
export default function DocumentGuideFrame({ state = 'idle' }) {
  return (
    <div className="pointer-events-none absolute inset-0 flex items-center justify-center p-6 sm:p-10">
      <div
        className={`relative aspect-[1.58/1] w-full max-w-lg rounded-xl border-4 border-dashed transition-colors duration-300 ${STATE_STYLES[state] || STATE_STYLES.idle}`}
      >
        <Corner className="left-0 top-0 -translate-x-1 -translate-y-1 border-l-4 border-t-4 rounded-tl-lg" />
        <Corner className="right-0 top-0 translate-x-1 -translate-y-1 border-r-4 border-t-4 rounded-tr-lg" />
        <Corner className="left-0 bottom-0 -translate-x-1 translate-y-1 border-l-4 border-b-4 rounded-bl-lg" />
        <Corner className="right-0 bottom-0 translate-x-1 translate-y-1 border-r-4 border-b-4 rounded-br-lg" />
      </div>
    </div>
  )
}

function Corner({ className }) {
  return <span className={`absolute h-6 w-6 border-white ${className}`} aria-hidden="true" />
}
