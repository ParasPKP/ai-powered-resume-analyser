import { Hono } from 'hono';

// ============================================================
// API routes are now handled by the Java Spring Boot backend.
// The Vite dev server proxies /api/* requests to localhost:8080.
// This file is kept as a stub for the Create.xyz Hono server.
// ============================================================

const API_BASENAME = '/api';
const api = new Hono();

console.log('API routes are served by Java backend at http://localhost:8080');

export { api, API_BASENAME };
