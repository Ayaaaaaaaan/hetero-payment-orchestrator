import axios from "axios";

const ORCHESTRATOR = "http://15.135.70.219:8050";  // ← my real EC2 Public IP
const EXECUTION    = "http://15.135.70.219:8051";

const orchestratorApi = axios.create({ baseURL: ORCHESTRATOR });
const executionApi = axios.create({ baseURL: EXECUTION });

export const api = {
  // Orchestrator
  createPayment: (payload) => orchestratorApi.post("/api/payments", payload),
  getAllTransactions: () => orchestratorApi.get("/api/transactions"),
  getTransactionById: (id) => orchestratorApi.get(`/api/transactions/${id}`),
  getByStatus: (status) =>
    orchestratorApi.get(`/api/transactions/status/${status}`),
  cancelTransaction: (id) =>
    orchestratorApi.post(`/api/transactions/${id}/cancel`),
  getFraudRules: () => orchestratorApi.get("/api/fraud/rules"),
  reloadFraudRules: () => orchestratorApi.post("/api/fraud/reload"),

  // Execution
  getCircuitBreakerStatus: () =>
    executionApi.get("/api/circuit-breaker/status"),
  simulateFailure: (provider) =>
    executionApi.post(`/api/execution/simulate-failure/${provider}`),
  clearFailure: (provider) =>
    executionApi.delete(`/api/execution/simulate-failure/${provider}`),
  getRecentTransactions: () => orchestratorApi.get("/api/transactions/recent"),
  getTransactionCount: () => orchestratorApi.get("/api/transactions/count"),
};