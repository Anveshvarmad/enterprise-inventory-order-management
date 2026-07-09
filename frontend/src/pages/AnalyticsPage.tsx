import { useEffect, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import { getAnalyticsDashboard } from "../lib/api";
import type { AnalyticsDashboard } from "../types";

function money(value: string | number) {
  return `$${Number(value).toLocaleString(undefined, {
    maximumFractionDigits: 2
  })}`;
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsDashboard | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    getAnalyticsDashboard()
      .then(setAnalytics)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load analytics"));
  }, []);

  const revenueTrend =
    analytics?.revenueTrend.map((point) => ({
      ...point,
      totalRevenueNumber: Number(point.totalRevenue)
    })) || [];

  const topProducts =
    analytics?.topProducts.map((product) => ({
      ...product,
      shortName:
        product.productName.length > 22
          ? product.productName.slice(0, 22) + "..."
          : product.productName
    })) || [];

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Analytics</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Business Intelligence Dashboard</h2>
        <p className="mt-2 text-slate-400">
          Revenue trends, order health, warehouse inventory, top products, and low-stock risk.
        </p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}

      {!analytics ? (
        <div className="card p-8 text-slate-400">Loading analytics...</div>
      ) : (
        <div className="space-y-6">
          <div className="grid gap-6 xl:grid-cols-2">
            <div className="card p-6">
              <h3 className="text-lg font-semibold text-white">Monthly Revenue Trend</h3>
              <p className="mt-1 text-sm text-slate-400">Total order value by month.</p>

              <div className="mt-6 h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={revenueTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="period" />
                    <YAxis />
                    <Tooltip formatter={(value) => money(Number(value))} />
                    <Line type="monotone" dataKey="totalRevenueNumber" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="card p-6">
              <h3 className="text-lg font-semibold text-white">Order Status Breakdown</h3>
              <p className="mt-1 text-sm text-slate-400">Current order distribution.</p>

              <div className="mt-6 h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={analytics.orderStatusBreakdown}
                      dataKey="count"
                      nameKey="status"
                      outerRadius={110}
                      label
                    />
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <div className="card p-6">
              <h3 className="text-lg font-semibold text-white">Top Products by Units Sold</h3>
              <p className="mt-1 text-sm text-slate-400">Best-performing products across non-cancelled orders.</p>

              <div className="mt-6 h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={topProducts}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="shortName" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="totalQuantitySold" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="card p-6">
              <h3 className="text-lg font-semibold text-white">Warehouse Inventory Distribution</h3>
              <p className="mt-1 text-sm text-slate-400">Quantity on hand by warehouse.</p>

              <div className="mt-6 h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={analytics.warehouseInventory}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="warehouseCode" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="quantityOnHand" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <div className="card overflow-hidden">
              <div className="border-b border-slate-800 p-5">
                <h3 className="text-lg font-semibold text-white">Top Products</h3>
                <p className="mt-1 text-sm text-slate-400">Revenue and quantity leaders.</p>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full min-w-[700px] text-left text-sm">
                  <thead className="border-b border-slate-800 bg-slate-950/70 text-xs uppercase tracking-wider text-slate-400">
                    <tr>
                      <th className="px-5 py-4">SKU</th>
                      <th className="px-5 py-4">Product</th>
                      <th className="px-5 py-4">Units</th>
                      <th className="px-5 py-4">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analytics.topProducts.map((product) => (
                      <tr key={product.productId} className="border-b border-slate-800/70">
                        <td className="px-5 py-4 font-mono text-cyan-300">{product.sku}</td>
                        <td className="px-5 py-4 text-white">{product.productName}</td>
                        <td className="px-5 py-4 text-slate-300">{product.totalQuantitySold}</td>
                        <td className="px-5 py-4 text-white">{money(product.totalRevenue)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card overflow-hidden">
              <div className="border-b border-slate-800 p-5">
                <h3 className="text-lg font-semibold text-white">Low-Stock Risk</h3>
                <p className="mt-1 text-sm text-slate-400">Products at or below reorder level.</p>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full min-w-[750px] text-left text-sm">
                  <thead className="border-b border-slate-800 bg-slate-950/70 text-xs uppercase tracking-wider text-slate-400">
                    <tr>
                      <th className="px-5 py-4">SKU</th>
                      <th className="px-5 py-4">Product</th>
                      <th className="px-5 py-4">Warehouse</th>
                      <th className="px-5 py-4">Available</th>
                      <th className="px-5 py-4">Reorder</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analytics.lowStockRisk.map((item) => (
                      <tr key={`${item.productId}-${item.warehouseId}`} className="border-b border-slate-800/70">
                        <td className="px-5 py-4 font-mono text-cyan-300">{item.sku}</td>
                        <td className="px-5 py-4 text-white">{item.productName}</td>
                        <td className="px-5 py-4 text-slate-300">{item.warehouseCode}</td>
                        <td className="px-5 py-4 text-red-300">{item.availableQuantity}</td>
                        <td className="px-5 py-4 text-slate-300">{item.reorderLevel}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {analytics.lowStockRisk.length === 0 && (
                  <div className="p-8 text-center text-slate-400">No low-stock risk found.</div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
