import { build } from 'esbuild';
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';

await rm('dist', { recursive: true, force: true });
await mkdir('dist/assets', { recursive: true });
await build({
  entryPoints: ['src.jsx'],
  bundle: true,
  platform: 'browser',
  format: 'esm',
  target: 'es2020',
  minify: true,
  outfile: 'dist/assets/main.js',
  logLevel: 'info',
});

const original = await readFile('index.html', 'utf8');
const oldScript = '<script type="module" src="/src.jsx"></script>';
if (!original.includes(oldScript)) {
  throw new Error('Entrada HTML inesperada; build interrompido.');
}
const html = original
  .replace(oldScript, '<script type="module" src="/assets/main.js"></script>')
  .replace('</head>', '<link rel="stylesheet" href="/assets/main.css"></head>');
await writeFile('dist/index.html', html, 'utf8');
