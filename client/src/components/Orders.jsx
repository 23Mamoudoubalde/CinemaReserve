import { useEffect, useState } from 'react';
import { money } from '../api.js';

export default function Orders({ api, version, onChange }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.orders().then(setData).catch((e) => setError(e.message));
  }, [api, version]);

  async function run(action) {
    setError('');
    try {
      await action();
      onChange();
    } catch (e) {
      setError(e.message);
    }
  }

  if (!data) return <p className="muted">{error || 'Loading orders…'}</p>;

  return (
    <section className="card receipt">
      <h2>My orders</h2>
      {error && <p className="error">{error}</p>}
      {data.orders.length === 0 && <p className="muted">No orders yet.</p>}
      {data.orders.map((o) => (
        <article key={o.id} className="order">
          <header>
            <strong>Order #{o.id} · Auditorium {o.auditorium}</strong>
            <button className="danger" onClick={() => run(() => api.cancelOrder(o.id))}>Cancel order</button>
          </header>
          <p className="muted">{o.adult} adult, {o.child} child, {o.senior} senior</p>
          <ul className="chips">
            {o.seats.map((s) => (
              <li key={s.label}>
                {s.label}
                <button aria-label={`Remove seat ${s.label}`} title="Remove this seat"
                  onClick={() => run(() => api.removeSeat(o.id, s.row, s.seat))}>×</button>
              </li>
            ))}
          </ul>
          <p className="order-total">Order total: {money(o.total)}</p>
        </article>
      ))}
      <p className="total">Customer total: {money(data.customerTotal)}</p>
    </section>
  );
}
