export default function ErrorState({ title = 'Something went wrong', message, onRetry }) {
  return (
    <div role="alert" className="flex flex-col items-center justify-center rounded-lg border border-signal-high/30 bg-signal-high/5 px-6 py-10 text-center">
      <h3 className="text-sm font-semibold text-signal-high">{title}</h3>
      {message && <p className="mt-1 max-w-sm text-sm text-ink-700">{message}</p>}
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="focus-ring mt-4 rounded-md border border-signal-high/40 bg-white px-4 py-2 text-sm font-medium text-signal-high hover:bg-signal-high/10"
        >
          Retry
        </button>
      )}
    </div>
  )
}
