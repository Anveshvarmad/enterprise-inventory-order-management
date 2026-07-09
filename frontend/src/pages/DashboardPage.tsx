import {
  AlertTriangle,
  Building2,
  DollarSign,
  Package,
  ShoppingCart,
  Truck,
  Users,
  Warehouse
} from "lucide-react";
import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import StatCard from "../components/StatCard";
import { getDashboardSummary } from "../lib/api";
import type { DashboardSummary } from "../types";

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    getDashboardSummary()
      .then(setSummary)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load dashboard"));
  }, []);

  const chartData = summary
    ? [
        { name: "Products", value: summary.totalProducts },
        { name: "Orders", value: summary.totalOrders },
        { name: "Customers", value: summary.totalCustomers },
        { name: "Warehouses", value: summary.totalWarehouses }
      ]
    : [];

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Operations</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Enterprise Dashboard</h2>
        <p className="mt-2 text-slate-400">
          Real-time view of products, inventory, orders, customers, and warehouse operations.
        </p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}

      {!summary ? (
        <div className="card p-8 text-slate-400">Loading dashboard...</div>
      ) : (
        <>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
            <StatCard title="Total Products" value={summary.totalProducts} icon={Package} subtitle="Catalog records" />
            <StatCard title="Low Stock Items" value={summary.lowStockItems} icon={AlertTriangle} subtitle="Need attention" />
            <StatCard title="Total Orders" value={summary.totalOrders} icon={ShoppingCart} subtitle="All customer orders" />
            <StatCard title="Order Value" value={`$${summary.totalOrderValue}`} icon={DollarSign} subtitle="Demo sales value" />
            <StatCard title="Customers" value={summary.totalCustomers} icon={Users} subtitle="Seeded customers" />
            <StatCard title="Warehouses" value={summary.totalWarehouses} icon={Warehouse} subtitle="Distribution locations" />
            <StatCard title="Pending Orders" value={summary.pendingOrders} icon={Building2} subtitle="Created or confirmed" />
            <StatCard title="Shipped Orders" value={summary.shippedOrders} icon={Truck} subtitle="Completed fulfillment" />
          </div>

          <div className="mt-8 grid gap-6 xl:grid-cols-3">
            <div className="card p-6 xl:col-span-2">
              <h3 className="text-lg font-semibold text-white">Business Overview</h3>
              <p className="mt-1 text-sm text-slate-400">GraphQL-powered summary from the Spring Boot backend.</p>

              <div className="mt-6 h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="value" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="card p-6">
              <h3 className="text-lg font-semibold text-white">System Notes</h3>
              <div className="mt-5 space-y-4 text-sm text-slate-300">
                <p>Backend: Java Spring Boot</p>
                <p>Database: PostgreSQL</p>
                <p>Cache: Redis</p>
                <p>API: REST + GraphQL</p>
                <p>Security: JWT role-based access</p>
                <p>Deployment: Docker-first foundation</p>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
