import { useEffect, useState } from 'react';
import { money } from '../api.js';

export default function AdminReport({ api }) {
  const [report, setReport] = useState(null);
  const [message, setMessage] = useState('');

  const load = () => api.report().then(setReport).catch((e) => setMessage(e.message));
  useEffect(() => { load(); }, [api]); // eslint-disable-line react-hooks/exhaustive-deps

  async function save() {
    try {
      const { saved } = await api.save();
      setMessage(`Saved ${saved.join(', ')}`);
    } catch (e) {
      setMessage(e.message);
    }
  }

  if (!report) return <p className="muted">{message || 'Loading report…'}</p>;

  const rows = [...report.auditoriums.map((r) => ({ name: `Auditorium ${r.auditorium}`, ...r })), { name: 'Total', ...report.total }];

  return (
    <section className="card">
      <div className="report-head">
        <h2>Sales report</h2>
        <div>
          <button onClick={load}>Refresh</button>
          <button className="primary" onClick={save}>Save seat files</button>
        </div>
      </div>
      <table>
        <thead>
          <tr><th></th><th>Open</th><th>Reserved</th><th>Adult</th><th>Child</th><th>Senior</th><th>Sales</th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.name} className={r.name === 'Total' ? 'total-row' : ''}>
              <th>{r.name}</th><td>{r.open}</td><td>{r.reserved}</td><td>{r.adult}</td>
              <td>{r.child}</td><td>{r.senior}</td><td>{money(r.sales)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {message && <p className="success">{message}</p>}
    </section>
  );
}
