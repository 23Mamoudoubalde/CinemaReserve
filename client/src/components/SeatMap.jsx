import { seatLetter } from '../api.js';

// Draws the auditorium. `selected` is { row, seat, count } for the block
// the customer is about to book; clicking a seat makes it the start seat.
export default function SeatMap({ layout, selected, onPick }) {
  const inBlock = (r, s) =>
    selected && selected.row === r && s >= selected.seat && s < selected.seat + selected.count;

  return (
    <div className="seatmap">
      <div className="screen">SCREEN</div>
      <div className="grid" style={{ '--cols': layout.seatsPerRow }}>
        <span />
        {layout.taken[0].map((_, s) => <span key={s} className="col-label">{seatLetter(s)}</span>)}
        {layout.taken.map((row, r) => (
          <Row key={r} row={row} r={r + 1} inBlock={inBlock} onPick={onPick} />
        ))}
      </div>
      <div className="legend">
        <span><i className="seat open" /> Open</span>
        <span><i className="seat picked" /> Your pick</span>
        <span><i className="seat taken" /> Taken</span>
      </div>
    </div>
  );
}

function Row({ row, r, inBlock, onPick }) {
  return (
    <>
      <span className="row-label">{r}</span>
      {row.map((taken, s) => {
        const label = `${r}${seatLetter(s)}`;
        const picked = inBlock(r, s);
        const cls = picked ? (taken ? 'clash' : 'picked') : taken ? 'taken' : 'open';
        return (
          <button
            key={s}
            className={`seat ${cls}`}
            disabled={taken && !picked}
            onClick={() => onPick(r, s)}
            aria-label={`Seat ${label}${taken ? ' (taken)' : ''}`}
            title={label}
          />
        );
      })}
    </>
  );
}
