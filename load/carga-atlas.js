// Prueba de carga ligera del atlas (RNF_005). Uso y umbrales: README, seccion "Pruebas de carga".
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
// Cuentas ya registradas, separadas por coma: correo:clave,correo:clave
const USUARIOS = (__ENV.USUARIOS || '')
  .split(',')
  .filter(Boolean)
  .map((par) => {
    const [correo, password] = par.split(':');
    return { correo, password };
  });
const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
  scenarios: {
    // 20 usuarios concurrentes navegando el atlas (todo GET autenticado, sin limite de tasa).
    lectura: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 20 },
        { duration: '15s', target: 0 },
      ],
      exec: 'lectura',
    },
    // 6 comentarios por minuto en total; el limite del servidor es 10 por usuario y minuto.
    comentarios: {
      executor: 'constant-arrival-rate',
      rate: 6,
      timeUnit: '1m',
      duration: '3m',
      preAllocatedVUs: 2,
      maxVUs: 5,
      exec: 'comentar',
    },
    // 4 logins por minuto; el limite del servidor es 10 por IP y minuto.
    login: {
      executor: 'constant-arrival-rate',
      rate: 4,
      timeUnit: '1m',
      duration: '3m',
      preAllocatedVUs: 1,
      maxVUs: 3,
      exec: 'loginCarga',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{scenario:lectura}': ['p(95)<800'],
    'http_req_duration{scenario:comentarios}': ['p(95)<1000'],
    'http_req_duration{scenario:login}': ['p(95)<1000'],
  },
};

const pick = (lista) => lista[Math.floor(Math.random() * lista.length)];
const auth = (token) => ({ headers: { Authorization: `Bearer ${token}` } });

function loginReq(u) {
  return http.post(`${BASE}/auth/login`, JSON.stringify({ correo: u.correo, password: u.password }), {
    headers: JSON_HEADERS,
  });
}

export function setup() {
  if (USUARIOS.length === 0) throw new Error('Define USUARIOS=correo:clave,... para correr la carga');
  // shortcut: un login por cuenta al arrancar; con mas de 6 cuentas el limite de login (10/min por IP) lo corta. Subir RATE_LIMIT_MAX en el entorno de prueba.
  const tokens = USUARIOS.map((u) => loginReq(u).json('accessToken'));
  if (tokens.some((t) => !t)) throw new Error('Login fallido en setup: revisa USUARIOS o el limite de login');
  const regiones = http.get(`${BASE}/regiones`, auth(tokens[0])).json();
  const ejercicios = http.get(`${BASE}/ejercicios`, auth(tokens[0])).json();
  if (!ejercicios.length) throw new Error('No hay fichas publicadas en el entorno de prueba');
  return { tokens, regiones, ids: ejercicios.map((e) => e.id) };
}

export function lectura(data) {
  const token = data.tokens[__VU % data.tokens.length];
  const region = pick(data.regiones);
  check(http.get(`${BASE}/regiones`, auth(token)), { 'regiones 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/ejercicios?region=${region.id}`, auth(token)), {
    'listado 200': (r) => r.status === 200,
  });
  check(http.get(`${BASE}/ejercicios/${pick(data.ids)}`, auth(token)), { 'ficha 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/ejercicios/buscar?q=cadera`, auth(token)), { 'busqueda 200': (r) => r.status === 200 });
  sleep(1);
}

export function comentar(data) {
  const token = data.tokens[__ITER % data.tokens.length];
  const id = pick(data.ids);
  const texto = `Comentario de prueba de carga ${__VU}-${__ITER}`;
  check(
    http.post(`${BASE}/ejercicios/${id}/comentarios`, JSON.stringify({ texto }), {
      headers: { ...JSON_HEADERS, Authorization: `Bearer ${token}` },
    }),
    { 'comentario 201': (r) => r.status === 201 },
  );
  check(http.get(`${BASE}/ejercicios/${id}/comentarios`, auth(token)), {
    'listado comentarios 200': (r) => r.status === 200,
  });
}

export function loginCarga() {
  const res = loginReq(USUARIOS[__ITER % USUARIOS.length]);
  check(res, { 'login 200': (r) => r.status === 200 });
}
