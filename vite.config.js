import {defineConfig} from "vite";

export default defineConfig({
	server: {
		cors: {
			// the origin you will be accessing via browser
			origin: 'http://localhost:4567',
		},
	},
	build: {
		// generate .vite/manifest.json in outDir
		manifest: true,
		rollupOptions: {
			// overwrite default .html entry
			input: 'modules/frontend/src/main/js/jakon.js',
			output: {
				dir: 'modules/backend/src/main/resources/static/jakon/vite',
				entryFileNames: 'jakon.js',
				assetFileNames: (assetInfo) => {
					if (assetInfo.name === 'style.css') return 'jakon.css';
					return '[name].[ext]';
				},
			}
		},
		emptyOutDir: false,
		cssCodeSplit: false,
	},
});

