import { NavLink } from 'react-router-dom'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: DashboardIcon },
  { to: '/new-scan', label: 'New Scan', icon: ScanIcon },
  { to: '/history', label: 'Scan History', icon: HistoryIcon },
  { to: '/reports', label: 'Reports', icon: ReportIcon },
  { to: '/audit', label: 'Audit', icon: AuditIcon },
  { to: '/settings', label: 'Settings', icon: SettingsIcon }
]

export default function Sidebar({ open, onNavigate }) {
  return (
    <aside
      className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col border-r border-ink-700/60 bg-ink-950 transition-transform duration-200 lg:static lg:translate-x-0 ${
        open ? 'translate-x-0' : '-translate-x-full'
      }`}
    >
      <div className="flex h-16 items-center gap-2 border-b border-ink-700/60 px-5">
        <MarkIcon />
        <div>
          <div className="text-sm font-semibold tracking-tight text-white">ForensiQ</div>
          <div className="text-[11px] text-steel-400">Document Screening</div>
        </div>
      </div>
      <nav className="flex-1 space-y-0.5 px-3 py-4">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            onClick={onNavigate}
            className={({ isActive }) =>
              `focus-ring flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-accent/15 text-white'
                  : 'text-steel-300 hover:bg-white/5 hover:text-white'
              }`
            }
          >
            <Icon className="h-4.5 w-4.5 shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-ink-700/60 px-5 py-4 text-[11px] leading-relaxed text-steel-500">
        Backend results are authoritative. This interface does not alter or recalculate any verdict.
      </div>
    </aside>
  )
}

function MarkIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <rect x="1" y="1" width="26" height="26" rx="6" stroke="#2E7DD1" strokeWidth="1.5" />
      <path d="M8 14l4 4 8-9" stroke="#2E7DD1" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function DashboardIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.5" />
    </svg>
  )
}
function ScanIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <path d="M4 8V5.5A1.5 1.5 0 015.5 4H8M16 4h2.5A1.5 1.5 0 0120 5.5V8M20 16v2.5a1.5 1.5 0 01-1.5 1.5H16M8 20H5.5A1.5 1.5 0 014 18.5V16" strokeLinecap="round" />
      <rect x="8.5" y="8.5" width="7" height="7" rx="1" />
    </svg>
  )
}
function HistoryIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <circle cx="12" cy="12" r="8" />
      <path d="M12 8v4l3 2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
function ReportIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <path d="M7 3.5h7l4 4V20a.5.5 0 01-.5.5h-11a.5.5 0 01-.5-.5V4a.5.5 0 01.5-.5z" />
      <path d="M9.5 12h5M9.5 15.5h5M9.5 8.5h2" strokeLinecap="round" />
    </svg>
  )
}
function AuditIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <path d="M12 3.5l7 3v5c0 4.5-3 7.5-7 8.5-4-1-7-4-7-8.5v-5l7-3z" strokeLinejoin="round" />
      <path d="M9.5 12l1.8 1.8L14.5 10" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
function SettingsIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" {...props}>
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 13a1.7 1.7 0 000-2l1.3-1.6-1.6-2.8-2 .5a1.7 1.7 0 01-1.7-1L15 3.4h-3.2l-.4 2.1a1.7 1.7 0 01-1.7 1l-2-.5-1.6 2.8L7.4 10a1.7 1.7 0 000 2l-1.3 1.6 1.6 2.8 2-.5a1.7 1.7 0 011.7 1l.4 2.1H15l.4-2.1a1.7 1.7 0 011.7-1l2 .5 1.6-2.8L19.4 13z" strokeLinejoin="round" />
    </svg>
  )
}
