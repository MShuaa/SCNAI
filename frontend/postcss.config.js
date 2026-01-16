module.exports = {
  plugins: {
    // Tailwind PostCSS plugin moved to a separate package.
    // Use the dedicated package as recommended by Vite/PostCSS overlay.
    // https://github.com/tailwindlabs/tailwindcss/tree/main/packages/postcss
    '@tailwindcss/postcss': {},
    autoprefixer: {}
  }
}
