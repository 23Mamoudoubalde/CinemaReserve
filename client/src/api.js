// Small wrapper around fetch for the Express API.

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.error ?? `Request failed (${status})`);
    this.status = status;
    this.body = body;
  }
}

export function createApi(token, onUnauthorized) {
  async function request(method, path, body) {
    const res = await fetch(`/api${path}`, {
      method,
      headers: {
        'content-type': 'application/json',
        ...(token && { authorization: `Bearer ${token}` }),
      },
      body: body && JSON.stringify(body),
    });
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    if (res.status === 401 && token) onUnauthorized?.();
    if (!res.ok) throw new ApiError(res.status, data);
    return data;
  }

  return {
    login: (username, password) => request('POST', '/login', { username, password }),
    logout: () => request('POST', '/logout'),
    auditoriums: () => request('GET', '/auditoriums'),
    seats: (id) => request('GET', `/auditoriums/${id}/seats`),
    orders: () => request('GET', '/orders'),
    createOrder: (order) => request('POST', '/orders', order),
    addTickets: (orderId, tickets) => request('POST', `/orders/${orderId}/tickets`, tickets),
    removeSeat: (orderId, row, seat) => request('DELETE', `/orders/${orderId}/seats/${row}/${seat}`),
    cancelOrder: (orderId) => request('DELETE', `/orders/${orderId}`),
    report: () => request('GET', '/admin/report'),
    save: () => request('POST', '/admin/save'),
  };
}

export const money = (n) => `$${n.toFixed(2)}`;
export const seatLetter = (i) => String.fromCharCode(65 + i);
