import { api } from "../api/client";

export default function FraudRulesPanel({ rules, onReload }) {
  const handleReload = async () => {
    await api.reloadFraudRules();
    onReload?.();
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold">Fraud Rules</h2>
        <button
          onClick={handleReload}
          className="text-xs bg-slate-800 hover:bg-slate-700 px-3 py-1 rounded"
        >
          Reload
        </button>
      </div>

      <div className="space-y-2 text-sm">
        {rules?.rules?.map((r) => (
          <div key={r.id} className="border border-slate-800 rounded p-3">
            <div className="flex justify-between">
              <span className="font-medium">{r.name}</span>
              <span
                className={`text-xs ${
                  r.enabled ? "text-green-400" : "text-slate-500"
                }`}
              >
                {r.enabled ? "ON" : "OFF"}
              </span>
            </div>
            <div className="text-xs text-slate-400 mt-1">{r.message}</div>
            <div className="text-xs text-slate-500 mt-1 font-mono">
              {JSON.stringify(r.params)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}