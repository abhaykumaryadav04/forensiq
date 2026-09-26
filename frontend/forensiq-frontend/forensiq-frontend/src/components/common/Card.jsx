export default function Card({ title, subtitle, actions, children, className = '', bodyClassName = '' }) {
  return (
    <div className={`rounded-lg border border-steel-200 bg-white shadow-card ${className}`}>
      {(title || actions) && (
        <div className="flex items-center justify-between border-b border-steel-100 px-5 py-4">
          <div>
            {title && <h3 className="text-sm font-semibold text-ink-900">{title}</h3>}
            {subtitle && <p className="mt-0.5 text-xs text-steel-500">{subtitle}</p>}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}
      <div className={`p-5 ${bodyClassName}`}>{children}</div>
    </div>
  )
}
