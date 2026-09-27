// Formats backend LocalDateTime (no timezone) as IST display
export function formatIST(dateStr) {
  if (!dateStr) return "-";

  // Backend sends "2026-09-26T16:35:57.520" (no timezone, already IST)
  const d = new Date(dateStr);

  const pad = (n) => String(n).padStart(2, "0");

  const day = pad(d.getDate());
  const months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
  const month = months[d.getMonth()];
  const hours = pad(d.getHours());
  const minutes = pad(d.getMinutes());
  const seconds = pad(d.getSeconds());

  return `${day} ${month}, ${hours}:${minutes}:${seconds}`;
}