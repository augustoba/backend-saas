// Prueba de carga simple (sin dependencias): N usuarios concurrentes durante D
// segundos, tráfico mezclado de los tres sistemas. Sólo lecturas, para no
// ensuciar la base. Uso:
//   node deploy/prueba-carga.mjs [base=http://localhost:8080] [usuarios=40] [segundos=30]
// En producción, correrla desde OTRA máquina contra https://<tienda>.miplataforma.com.
const BASE = process.argv[2] ?? 'http://localhost:8080';
const USUARIOS = Number(process.argv[3] ?? 40);
const SEGUNDOS = Number(process.argv[4] ?? 30);

async function login(tienda, dni, password) {
  const r = await fetch(`${BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Demo-Tenant': tienda },
    body: JSON.stringify({ dni, password }),
  });
  return 'Bearer ' + (await r.json()).token;
}

const eco = await login('estilos-pequenos', '11111111', 'ruth123');
const sa = await login('kiosco-prueba', '33756194', 'augusto123');
const resto = await login('parrilla-prueba', '30111222', 'parrilla123');

// [peso, nombre, tienda, path, token]
const MEZCLA = [
  [30, 'catálogo público', 'estilos-pequenos', '/api/products', null],
  [15, 'settings', 'estilos-pequenos', '/api/settings', null],
  [10, 'ficha de producto', 'estilos-pequenos', '/api/products/best-sellers?limit=8', null],
  [10, 'panel: pedidos', 'estilos-pequenos', '/api/admin/orders?page=0&size=20', eco],
  [15, 'POS: artículos', 'kiosco-prueba', '/api/pos/articulos', sa],
  [5, 'POS: caja', 'kiosco-prueba', '/api/pos/caja/actual', sa],
  [10, 'resto: salones', 'parrilla-prueba', '/api/restaurante/salones', resto],
  [5, 'resto: cocina', 'parrilla-prueba', '/api/restaurante/kds/pendientes', resto],
];
const total = MEZCLA.reduce((s, m) => s + m[0], 0);
const elegir = () => {
  let x = Math.random() * total;
  for (const m of MEZCLA) if ((x -= m[0]) < 0) return m;
  return MEZCLA[0];
};

const tiempos = [];
const porTipo = {};
let errores = 0;
const fin = Date.now() + SEGUNDOS * 1000;

async function usuario() {
  while (Date.now() < fin) {
    const [, nombre, tienda, path, token] = elegir();
    const t0 = performance.now();
    try {
      const r = await fetch(BASE + path, {
        headers: { 'X-Demo-Tenant': tienda, 'Accept-Encoding': 'gzip', ...(token ? { Authorization: token } : {}) },
      });
      await r.arrayBuffer();
      if (r.status >= 400) errores++;
    } catch {
      errores++;
    }
    const ms = performance.now() - t0;
    tiempos.push(ms);
    (porTipo[nombre] ??= []).push(ms);
  }
}

const p = (arr, q) => {
  const s = [...arr].sort((a, b) => a - b);
  return s[Math.min(s.length - 1, Math.floor(q * s.length))]?.toFixed(0);
};
await Promise.all(Array.from({ length: USUARIOS }, usuario));
console.log(`${USUARIOS} usuarios, ${SEGUNDOS}s → ${tiempos.length} requests (${(tiempos.length / SEGUNDOS).toFixed(0)}/s), errores: ${errores}`);
console.log(`latencia ms  p50 ${p(tiempos, 0.5)}  p95 ${p(tiempos, 0.95)}  p99 ${p(tiempos, 0.99)}`);
for (const [n, arr] of Object.entries(porTipo)) console.log(`  ${n.padEnd(18)} ${String(arr.length).padStart(6)}  p50 ${p(arr, 0.5)}  p95 ${p(arr, 0.95)}`);
