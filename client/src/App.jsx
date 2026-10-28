import { useMemo, useState } from 'react';
import { createApi } from './api.js';
import Login from './components/Login.jsx';
import Booking from './components/Booking.jsx';
import Orders from './components/Orders.jsx';
import AdminReport from './components/AdminReport.jsx';

export default function App() {
  const [session, setSession] = useState(null); // { token, username, role }
  const [tab, setTab] = useState('book');
  // Bumped after any booking change so other views reload
  const [version, setVersion] = useState(0);
  const refresh = () => setVersion((v) => v + 1);

  const api = useMemo(() => createApi(session?.token, () => setSession(null)), [session]);

  async function logout() {
    try { await api.logout(); } catch { /* already logged out */ }
    setSession(null);
    setTab('book');
  }

  if (!session) return <Login api={api} onLogin={setSession} />;

  const isAdmin = session.role === 'admin';

  return (
    <div className="app">
      <header className="topbar">
        <h1>CinemaReserve</h1>
        {!isAdmin && (
          <nav>
            <button className={tab === 'book' ? 'active' : ''} onClick={() => setTab('book')}>Book seats</button>
            <button className={tab === 'orders' ? 'active' : ''} onClick={() => setTab('orders')}>My orders</button>
          </nav>
        )}
        <div className="user">
          <span>{session.username}</span>
          <button className="link" onClick={logout}>Log out</button>
        </div>
      </header>

      <main>
        {isAdmin && <AdminReport api={api} />}
        {!isAdmin && tab === 'book' && <Booking api={api} version={version} onChange={refresh} />}
        {!isAdmin && tab === 'orders' && <Orders api={api} version={version} onChange={refresh} />}
      </main>
    </div>
  );
}
