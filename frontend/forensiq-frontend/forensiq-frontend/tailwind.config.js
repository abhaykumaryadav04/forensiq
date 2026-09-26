/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          950: '#0A1320',
          900: '#0D1B2E',
          800: '#132A47',
          700: '#1B3A5C',
          600: '#27507A',
          500: '#3A6896'
        },
        steel: {
          50: '#F4F7FA',
          100: '#E7EDF3',
          200: '#CBD8E5',
          300: '#A9BCCF',
          400: '#7C93AC',
          500: '#5A7288',
          600: '#465A6E'
        },
        signal: {
          verified: '#1B7A4A',
          low: '#2E8B57',
          review: '#B8860B',
          suspicious: '#C2410C',
          high: '#B91C1C',
          unknown: '#5A7288'
        },
        accent: {
          DEFAULT: '#2E7DD1',
          dim: '#1E5A98'
        }
      },
      fontFamily: {
        sans: ['"IBM Plex Sans"', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace']
      },
      boxShadow: {
        card: '0 1px 2px rgba(10, 19, 32, 0.06), 0 1px 1px rgba(10, 19, 32, 0.04)'
      }
    }
  },
  plugins: []
}
