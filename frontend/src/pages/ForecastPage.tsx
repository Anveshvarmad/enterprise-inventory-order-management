import { useEffect, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import { getDemandForecast } from "../lib/api";
import type { DemandForecast } from "../types";

function riskClass(risk: string) {
  if (risk === "HIGH") {
    return "bg-red-500/10 text-red-300";
  }

  if (risk === "MEDIUM") {
    return "bg-yellow-500/10 text-yellow-300";
  }

  return "bg-emerald-500/10 text-emerald-300";
}

export default function ForecastPage() {
  const [forecast, setForecast] = useState<DemandForecast | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    getDemandForecast()
      .then(setForecast)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load forecast"));
  }, []);

  const chartData =
    forecast?.forecasts.slice(0, 10).map((item) => ({
      sku: item.sku,
      predictedDemand7Days: item.predictedDemand7Days,
      availableQuantity: item.availableQuantity
    })) || [];

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Machine Learning</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Demand Forecasting</h2>
        <p className="mt-2 text-slate-400">
          Predicts 7-day demand, stockout risk, and reorder recommendations using historical order activity.
        </p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}

      {!forecast ? (
        <div className="card p-8 text-slate-400">Loading ML demand forecast...</div>
      ) : (
        <div className="space-y-6">
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
            <div className="card p-5">
              <p className="text-sm text-slate-400">Model</p>
              <h3 className="mt-3 text-2xl font-bold text-white">{forecast.modelName}</h3>
              <p className="mt-2 text-xs text-slate-500">{forecast.modelVersion}</p>
            </div>

            <div className="card p-5">
              <p className="text-sm text-slate-400">Items Scored</p>
              <h3 className="mt-3 text-3xl font-bold text-white">{forecast.totalItemsScored}</h3>
              <p className="mt-2 text-xs text-slate-500">Product + warehouse combinations</p>
            </div>

            <div className="card p-5">
              <p className="text-sm text-slate-400">High Risk</p>
              <h3 className="mt-3 text-3xl font-bold text-red-300">{forecast.highRiskItems}</h3>
              <p className="mt-2 text-xs text-slate-500">Needs restock soon</p>
            </div>

            <div className="card p-5">
              <p className="text-sm text-slate-400">Medium Risk</p>
              <h3 className="mt-3 text-3xl font-bold text-yellow-300">{forecast.mediumRiskItems}</h3>
              <p className="mt-2 text-xs text-slate-500">Watch closely</p>
            </div>
          </div>

          <div className="card p-6">
            <h3 className="text-lg font-semibold text-white">Top Forecasted Demand</h3>
            <p className="mt-1 text-sm text-slate-400">
              Predicted 7-day demand compared with current available stock.
            </p>

            <div className="mt-6 h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="sku" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="predictedDemand7Days" />
                  <Bar dataKey="availableQuantity" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="card overflow-hidden">
            <div className="border-b border-slate-800 p-5">
              <h3 className="text-lg font-semibold text-white">Forecast Details</h3>
              <p className="mt-1 text-sm text-slate-400">
                Ranked by risk level and estimated stockout timeline.
              </p>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-[1100px] text-left text-sm">
                <thead className="border-b border-slate-800 bg-slate-950/70 text-xs uppercase tracking-wider text-slate-400">
                  <tr>
                    <th className="px-5 py-4">SKU</th>
                    <th className="px-5 py-4">Product</th>
                    <th className="px-5 py-4">Warehouse</th>
                    <th className="px-5 py-4">Available</th>
                    <th className="px-5 py-4">7-Day Demand</th>
                    <th className="px-5 py-4">Daily Demand</th>
                    <th className="px-5 py-4">Days Until Stockout</th>
                    <th className="px-5 py-4">Recommended Reorder</th>
                    <th className="px-5 py-4">Risk</th>
                  </tr>
                </thead>
                <tbody>
                  {forecast.forecasts.map((item) => (
                    <tr key={`${item.productId}-${item.warehouseId}`} className="border-b border-slate-800/70">
                      <td className="px-5 py-4 font-mono text-cyan-300">{item.sku}</td>
                      <td className="px-5 py-4 text-white">{item.productName}</td>
                      <td className="px-5 py-4 text-slate-300">{item.warehouseCode}</td>
                      <td className="px-5 py-4 text-slate-300">{item.availableQuantity}</td>
                      <td className="px-5 py-4 text-white">{item.predictedDemand7Days}</td>
                      <td className="px-5 py-4 text-slate-300">{item.predictedDailyDemand}</td>
                      <td className="px-5 py-4 text-slate-300">
                        {item.estimatedDaysUntilStockout >= 999 ? "Stable" : item.estimatedDaysUntilStockout}
                      </td>
                      <td className="px-5 py-4 text-white">{item.recommendedReorderQuantity}</td>
                      <td className="px-5 py-4">
                        <span className={`badge ${riskClass(item.riskLevel)}`}>{item.riskLevel}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {forecast.forecasts.length === 0 && (
                <div className="p-8 text-center text-slate-400">No forecast data found.</div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
