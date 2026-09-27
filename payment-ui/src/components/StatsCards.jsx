export default function StatsCards({ transactions, totalCount }) {
  const total = transactions.length;
  const success = transactions.filter((t) => t.status === "SUCCESS").length;
  const pending = transactions.filter((t) => t.status === "PENDING").length;
  const blocked = transactions.filter((t) => t.status === "BLOCKED").length;
  const successRate = total ? ((success / total) * 100).toFixed(1) : "0.0";

  const cards = [
    { label: "Total", value: totalCount, color: "text-slate-100" },
    { label: "Success Rate", value: `${successRate}%`, color: "text-green-400" },
    { label: "Pending", value: pending, color: "text-yellow-400" },
    { label: "Blocked", value: blocked, color: "text-red-400" },
  ];

  return (
    <div className="grid grid-cols-4 gap-4 mb-6">
      {cards.map((c) => (
        <div
          key={c.label}
          className="bg-slate-900 border border-slate-800 rounded-lg p-4"
        >
          <div className="text-sm text-slate-400 mb-1">{c.label}</div>
          <div className={`text-3xl font-bold ${c.color}`}>{c.value}</div>
        </div>
      ))}
    </div>
  );
}