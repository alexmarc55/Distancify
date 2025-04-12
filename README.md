FRONTEND
The src files contains the react frontend app. 
Steps:
1. Create the react app folder using: npx create-react-app my-react-app
2. cd my-react-app
3. To install tailwind: npm install tailwindcss postcss autoprefixer
4. npx tailwindcss init -p
5. In tailwind.config.js : paste this:
tailwind.config.js
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
6. Replace the src files with this one
7. Drag the images folder into public
8. To run the project: npm run dev
