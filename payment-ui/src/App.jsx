import { useEffect, useState } from "react";
import { api } from "./api/client";
import StatsCards from "./components/StatsCards";
import PaymentForm from "./components/PaymentForm";
import TransactionTable from "./components/TransactionTable";
import CircuitBreakerPanel from "./components/CircuitBreakerPanel";
import FraudRulesPanel from "./components/FraudRulesPanel";

export default function App() {
  const [transactions, setTransactions] = useState([]);
  const [circuitStatus, setCircuitStatus] = useState({});
  const [fraudRules, setFraudRules] = useState(null);
  const [totalCount, setTotalCount] = useState(0);

  const loadAll = async () => {
  const [txns, circuits, rules, count] = await Promise.allSettled([
    api.getRecentTransactions(),
    api.getCircuitBreakerStatus(),
    api.getFraudRules(),
    api.getTransactionCount(),
  ]);

  if (txns.status === "fulfilled") {
    setTransactions([...txns.value.data]);
  } else {
    console.warn("Transactions fetch failed:", txns.reason?.message);
  }

  if (circuits.status === "fulfilled") {
    setCircuitStatus(circuits.value.data);
  } else {
    console.warn("Circuit status fetch failed:", circuits.reason?.message);
  }

  if (rules.status === "fulfilled") {
    setFraudRules(rules.value.data);
  } else {
    console.warn("Fraud rules fetch failed:", rules.reason?.message);
  }

  if (count.status === "fulfilled") {
    setTotalCount(count.value.data.total);
  } else {
    console.warn("Count fetch failed:", count.reason?.message);
  }
};

  useEffect(() => {
    loadAll();
    const interval = setInterval(loadAll, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen p-6 max-w-7xl mx-auto">
      <header className="mb-6">
        <h1 className="text-2xl font-bold">Payment Orchestrator</h1>
        <p className="text-slate-400 text-sm">
        Microservices demo · Auto-refresh every 3s
      </p>
      </header>

      <StatsCards transactions={transactions} totalCount={totalCount} />

      <div className="grid grid-cols-3 gap-6">
        {/* Left column */}
        <div className="space-y-6">
          <PaymentForm onPaymentComplete={loadAll} />
          <FraudRulesPanel rules={fraudRules} onReload={loadAll} />
        </div>

        {/* Right column - 2 wide */}
        <div className="col-span-2 space-y-6">
          <CircuitBreakerPanel status={circuitStatus} onRefresh={loadAll} />
          <TransactionTable
            transactions={transactions}
            onRefresh={loadAll}
          />
        </div>
      </div>
    </div>
  );
}