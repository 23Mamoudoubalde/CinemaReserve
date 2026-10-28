import { useEffect, useState } from 'react';
import { money, seatLetter } from '../api.js';
import SeatMap from './SeatMap.jsx';

const PRICES = { adult: 10, child: 5, senior: 7.5 };

export default function Booking({ api, version, onChange }) {
  const [auditoriums, setAuditoriums] = useState([]);
  const [auditorium, setAuditorium] = useState(1);
  const [layout, setLayout] = useState(null);
  const [tickets, setTickets] = useState({ adult: 1, child: 0, senior: 0 });
  const [start, setStart] = useState(null); // { row, seat }
  const [orders, setOrders] = useState([]);
  const [addTo, setAddTo] = useState(''); // '' = new order, otherwise order id
  const [suggestion, setSuggestion] = useState(null);
  const [message, setMessage] = useState(null); // { type, text }

  const count = tickets.adult + tickets.child + tickets.senior;
  const total = Object.entries(tickets).reduce((sum, [k, n]) => sum + n * PRICES[k], 0);
  const ordersHere = orders.filter((o) => o.auditorium === auditorium);

  useEffect(() => {
    api.auditoriums().then(setAuditoriums).catch((e) => setMessage({ type: 'error', text: e.message }));
    api.orders().then((d) => setOrders(d.orders)).catch(() => {});
  }, [api, version]);

  useEffect(() => {
    setLayout(null);
    api.seats(auditorium).then(setLayout).catch((e) => setMessage({ type: 'error', text: e.message }));
  }, [api, auditorium, version]);

  function switchAuditorium(id) {
    setAuditorium(id);
    setStart(null);
    setAddTo('');
    setSuggestion(null);
  }

  function setTicket(type, value) {
    setTickets((t) => ({ ...t, [type]: Math.max(0, Math.min(10, Number(value) || 0)) }));
    setSuggestion(null);
  }

  async function book(at = start) {
    setMessage(null);
    setSuggestion(null);
    try {
      const body = { row: at.row, seat: at.seat, ...tickets };
      const order = addTo
        ? await api.addTickets(Number(addTo), body)
        : await api.createOrder({ auditorium, ...body });
      setMessage({
        type: 'ok',
        text: `Booked ${order.seats.map((s) => s.label).join(', ')} in Auditorium ${order.auditorium}. Order total ${money(order.total)}.`,
      });
      setStart(null);
      onChange();
    } catch (err) {
      if (err.status === 409 && err.body?.bestAvailable) {
        setSuggestion(err.body.bestAvailable);
      } else {
        setMessage({ type: 'error', text: err.message });
      }
    }
  }

  const blockLabel = (s) => `${s.row}${seatLetter(s.seat)}–${s.row}${seatLetter(s.seat + count - 1)}`;

  return (
    <div className="booking">
      <section className="card">
        <div className="tabs">
          {auditoriums.map((a) => (
            <button key={a.id} className={a.id === auditorium ? 'active' : ''} onClick={() => switchAuditorium(a.id)}>
              Auditorium {a.id} <small>{a.open} open</small>
            </button>
          ))}
        </div>
        {layout ? (
          <SeatMap
            layout={layout}
            selected={start && { ...start, count }}
            onPick={(row, seat) => { setStart({ row, seat }); setSuggestion(null); setMessage(null); }}
          />
        ) : <p className="muted">Loading seats…</p>}
      </section>

      <aside className="card panel">
        <h2>Your tickets</h2>
        {['adult', 'child', 'senior'].map((type) => (
          <label key={type} className="ticket">
            <span>{type[0].toUpperCase() + type.slice(1)} <small className="muted">{money(PRICES[type])}</small></span>
            <input type="number" min="0" max="10" value={tickets[type]} onChange={(e) => setTicket(type, e.target.value)} />
          </label>
        ))}
        <p className="total">{count} ticket{count === 1 ? '' : 's'} · {money(total)}</p>

        {ordersHere.length > 0 && (
          <label>
            Book as
            <select value={addTo} onChange={(e) => setAddTo(e.target.value)}>
              <option value="">A new order</option>
              {ordersHere.map((o) => (
                <option key={o.id} value={o.id}>Add to order #{o.id} ({o.seats.map((s) => s.label).join(', ')})</option>
              ))}
            </select>
          </label>
        )}

        <p className="muted">
          {start ? `Seats ${blockLabel(start)}` : 'Click a seat on the map to choose where your group starts.'}
        </p>
        <button className="primary" disabled={!start || count === 0} onClick={() => book()}>
          Book {count || ''} seat{count === 1 ? '' : 's'}
        </button>

        {suggestion && (
          <div className="notice">
            <p>Those seats aren't available. Best open seats: <strong>{blockLabel(suggestion)}</strong></p>
            <button className="primary" onClick={() => { setStart(suggestion); book(suggestion); }}>Book these instead</button>
          </div>
        )}
        {message && <p className={message.type === 'ok' ? 'success' : 'error'} role="status">{message.text}</p>}
      </aside>
    </div>
  );
}
