import { api } from "../api/client";

export default function CircuitBreakerPanel({ status, onRefresh }) {
  const stateColor = {
    CLOSED: "text-green-400",
    OPEN: "text-red-400",
    HALF_OPEN: "text-yellow-400",
  };

  const toggleFailure = async (provider, isFailing) => {
    try {
      if (isFailing) {
        await api.clearFailure(provider);
      } else {
        await api.simulateFailure(provider);
      }
      onRefresh?.();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <h2 className="text-lg font-semibold mb-4">Circuit Breakers</h2>
      <div className="space-y-3">
        {Object.entries(status).map(([provider, info]) => {
          const isFailing = info.state !== "CLOSED";
          return (
            <div
              key={provider}
              className="flex items-center justify-between border border-slate-800 rounded p-3"
            >
              <div>
                <div className="font-medium capitalize">{provider}</div>
                <div className={`text-sm ${stateColor[info.state]}`}>
                  {info.state} · {info.failureRate}
                </div>
                <div className="text-xs text-slate-500">
                  {info.successfulCalls} ok / {info.failedCalls} fail
                </div>
              </div>
              <button
                onClick={() => toggleFailure(provider, isFailing)}
                className={`text-xs px-3 py-1 rounded transition ${
                  isFailing
                    ? "bg-green-700 hover:bg-green-600"
                    : "bg-red-800 hover:bg-red-700"
                }`}
              >
                {isFailing ? "Recover" : "Simulate Failure"}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}