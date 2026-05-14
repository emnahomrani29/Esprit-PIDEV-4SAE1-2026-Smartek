/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // SMARTEK Brand Colors - Orange & Bleu
        primary: '#F25C2B',      // Orange primaire - énergique
        accent: '#2563EB',       // Bleu accent - professionnel
        background: '#FFF8F5',   // Fond crème léger
        text: '#1C1917',         // Texte noir élégant
        
        // Variations
        'primary-light': '#FF7A50',
        'primary-dark': '#D94A1F',
        'accent-light': '#3B82F6',
        'accent-dark': '#1D4ED8',
        
        // Anciens noms pour compatibilité
        orange: '#F25C2B',
        success: '#10B981',
        cream: '#FFF8F5',
      },
      boxShadow: {
        'mentor-shadow': '0px 4px 20px rgba(242, 92, 43, 0.1)',
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'card-hover': '0 20px 25px -5px rgba(242, 92, 43, 0.1), 0 10px 10px -5px rgba(242, 92, 43, 0.04)',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

