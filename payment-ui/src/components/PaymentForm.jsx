import { useState } from "react";
import { api } from "../api/client";
import { formatIST } from "../utils/dateUtils";

export default function PaymentForm({ onPaymentComplete }) {
  const [form, setForm] = useState({
    userId: "user_demo",
    amount: 5000,
    currency: "INR",
    provider: "auto",
    cardNumber: "4111111111111111",
    cvv: "123",
    expiryMonth: "12",
    expiryYear: "2028",
    customerEmail: "demo@example.com",
    customerName: "Demo User",
  });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    try {
      const payload = { ...form, amount: parseFloat(form.amount) };
      const res = await api.createPayment(payload);
      setResult(res.data);
      onPaymentComplete?.();
    } catch (err) {
      setResult({
        status: "ERROR",
        message: err.response?.data?.message || err.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const statusColor = {
    SUCCESS: "text-green-400 border-green-800 bg-green-950",
    FAILED: "text-red-400 border-red-800 bg-red-950",
    PENDING: "text-yellow-400 border-yellow-800 bg-yellow-950",
    BLOCKED: "text-red-400 border-red-800 bg-red-950",
    ERROR: "text-red-400 border-red-800 bg-red-950",
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <h2 className="text-lg font-semibold mb-4">Make a Payment</h2>

      <form onSubmit={handleSubmit} className="space-y-3">
        <div className="grid grid-cols-2 gap-3">
          <input
            name="userId"
            value={form.userId}
            onChange={handleChange}
            placeholder="User ID"
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          />
          <input
            name="amount"
            type="number"
            value={form.amount}
            onChange={handleChange}
            placeholder="Amount"
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <select
            name="currency"
            value={form.currency}
            onChange={handleChange}
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          >
            <option value="INR">INR</option>
            <option value="USD">USD</option>
          </select>
          <select
            name="provider"
            value={form.provider}
            onChange={handleChange}
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          >
            <option value="auto">Auto (best provider)</option>
            <option value="razorpay">Razorpay</option>
            <option value="stripe">Stripe</option>
            <option value="paypal">PayPal</option>
          </select>
        </div>

        <input
          name="cardNumber"
          value={form.cardNumber}
          onChange={handleChange}
          placeholder="Card Number"
          className="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
        />

        <div className="grid grid-cols-3 gap-3">
          <input
            name="cvv"
            value={form.cvv}
            onChange={handleChange}
            placeholder="CVV"
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          />
          <input
            name="expiryMonth"
            value={form.expiryMonth}
            onChange={handleChange}
            placeholder="MM"
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          />
          <input
            name="expiryYear"
            value={form.expiryYear}
            onChange={handleChange}
            placeholder="YYYY"
            className="bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-slate-700 text-white font-medium py-2 rounded transition"
        >
          {loading ? "Processing..." : "Pay Now"}
        </button>
      </form>

      {result && (
        <div
          className={`mt-4 border rounded p-3 text-sm ${
            statusColor[result.status] || statusColor.ERROR
          }`}
        >
          <div className="font-semibold">Status: {result.status}</div>
          {result.provider && <div>Provider: {result.provider}</div>}
          {result.providerTransactionId && (
            <div className="font-mono text-xs">
              {result.providerTransactionId}
            </div>
          )}
          {result.message && (
            <div className="text-xs mt-1">{result.message}</div>
          )}
            {result.timestamp && (
            <div className="text-xs text-slate-400 mt-1">
                {formatIST(new Date(result.timestamp).toISOString())}
            </div>
            )}
        </div>
      )}
    </div>
  );
}