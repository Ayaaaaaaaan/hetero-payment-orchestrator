import { api } from "../api/client";
import { formatIST } from "../utils/dateUtils";

export default function TransactionTable({ transactions, onRefresh }) {
  const statusColor = {
    SUCCESS: "bg-green-900 text-green-300",
    FAILED: "bg-red-900 text-red-300",
    PENDING: "bg-yellow-900 text-yellow-300",
    PROCESSING: "bg-blue-900 text-blue-300",
    BLOCKED: "bg-red-900 text-red-300",
    EXPIRED: "bg-slate-700 text-slate-300",
    CANCELLED: "bg-slate-700 text-slate-300",
  };

  const handleCancel = async (id) => {
    if (!confirm(`Cancel transaction ${id}?`)) return;
    try {
      await api.cancelTransaction(id);
      onRefresh?.();
    } catch (err) {
      alert(err.response?.data?.error || "Cancel failed");
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <h2 className="text-lg font-semibold mb-4">Transaction History</h2>
      <div className="overflow-y-auto max-h-[500px]">
        <table className="w-full text-sm">
          <thead className="text-slate-400 text-left sticky top-0 bg-slate-900">
            <tr>
              <th className="pb-2">Time (IST)</th>
              <th className="pb-2">TXN ID</th>
              <th className="pb-2">Amount</th>
              <th className="pb-2">Provider</th>
              <th className="pb-2">Status</th>
              <th className="pb-2">Retries</th>
              <th className="pb-2"></th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr
                key={t.id}
                className="border-t border-slate-800 hover:bg-slate-800/50"
              >
                <td className="py-2 text-slate-400 font-mono text-xs">
                  {formatIST(t.createdAt)}
                </td>
                <td className="py-2 font-mono text-xs">{t.transactionId}</td>
                <td className="py-2">
                  {t.currency} {t.amount}
                </td>
                <td className="py-2 capitalize">{t.provider}</td>
                <td className="py-2">
                  <span
                    className={`px-2 py-0.5 rounded text-xs ${
                      statusColor[t.status] || "bg-slate-700"
                    }`}
                  >
                    {t.status}
                  </span>
                </td>
                <td className="py-2 text-slate-400">{t.retryCount ?? 0}</td>
                <td className="py-2">
                  {(t.status === "PENDING" || t.status === "PROCESSING") && (
                    <button
                      onClick={() => handleCancel(t.transactionId)}
                      className="text-xs text-red-400 hover:text-red-300"
                    >
                      Cancel
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr>
                <td colSpan="7" className="py-4 text-center text-slate-500">
                  No transactions yet
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}