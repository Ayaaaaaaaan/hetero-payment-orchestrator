import axios from "axios";

const ORCHESTRATOR = "http://localhost:8050";
const EXECUTION = "http://localhost:8051";

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