import { FormEvent, useEffect, useState } from "react";
import {
  cancelOrder,
  createOrder,
  getCustomers,
  getOrders,
  getProducts,
  getWarehouses,
  markOrderPaid,
  shipOrder
} from "../lib/api";
import type { Customer, Order, Product, Warehouse } from "../types";

function statusClass(status: string) {
  if (status === "PAID" || status === "SHIPPED" || status === "DELIVERED") {
    return "bg-emerald-500/10 text-emerald-300";
  }

  if (status === "CANCELLED" || status === "FAILED") {
    return "bg-red-500/10 text-red-300";
  }

  return "bg-yellow-500/10 text-yellow-300";
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [customerId, setCustomerId] = useState("");
  const [productId, setProductId] = useState("");
  const [warehouseId, setWarehouseId] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [notes, setNotes] = useState("");

  async function loadData() {
    const [orderData, customerData, productData, warehouseData] = await Promise.all([
      getOrders(),
      getCustomers(),
      getProducts(),
      getWarehouses()
    ]);

    setOrders(orderData.content);
    setCustomers(customerData);
    setProducts(productData.content);
    setWarehouses(warehouseData);

    if (!customerId && customerData.length > 0) {
      setCustomerId(String(customerData[0].id));
    }

    if (!productId && productData.content.length > 0) {
      setProductId(String(productData.content[0].id));
    }

    if (!warehouseId && warehouseData.length > 0) {
      setWarehouseId(String(warehouseData[0].id));
    }
  }

  useEffect(() => {
    loadData().catch((err) => setError(err instanceof Error ? err.message : "Failed to load orders"));
  }, []);

  async function handleCreateOrder(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      await createOrder({
        customerId: Number(customerId),
        items: [
          {
            productId: Number(productId),
            warehouseId: Number(warehouseId),
            quantity: Number(quantity)
          }
        ],
        notes
      });

      setQuantity("1");
      setNotes("");
      setMessage("Order created and inventory reserved successfully.");
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create order");
    }
  }

  async function runOrderAction(action: () => Promise<Order>, successMessage: string) {
    setError("");
    setMessage("");

    try {
      await action();
      setMessage(successMessage);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Order action failed");
    }
  }

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Fulfillment</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Orders</h2>
        <p className="mt-2 text-slate-400">Create orders, reserve inventory, mark payments, ship, and cancel.</p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}
      {message && <div className="mb-6 rounded-xl bg-emerald-500/10 p-4 text-emerald-300">{message}</div>}

      <form onSubmit={handleCreateOrder} className="card mb-8 grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-6">
        <select className="input" value={customerId} onChange={(e) => setCustomerId(e.target.value)} required>
          {customers.map((customer) => (
            <option key={customer.id} value={customer.id}>
              {customer.fullName}
            </option>
          ))}
        </select>

        <select className="input" value={productId} onChange={(e) => setProductId(e.target.value)} required>
          {products.map((product) => (
            <option key={product.id} value={product.id}>
              {product.sku} - {product.name}
            </option>
          ))}
        </select>

        <select className="input" value={warehouseId} onChange={(e) => setWarehouseId(e.target.value)} required>
          {warehouses.map((warehouse) => (
            <option key={warehouse.id} value={warehouse.id}>
              {warehouse.code}
            </option>
          ))}
        </select>

        <input className="input" type="number" min="1" placeholder="Quantity" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
        <input className="input" placeholder="Notes" value={notes} onChange={(e) => setNotes(e.target.value)} />

        <button className="btn-primary">Create Order</button>
      </form>

      <div className="space-y-5">
        {orders.map((order) => (
          <div key={order.id} className="card p-5">
            <div className="flex flex-col justify-between gap-4 border-b border-slate-800 pb-5 md:flex-row md:items-start">
              <div>
                <p className="font-mono text-sm text-cyan-300">{order.orderNumber}</p>
                <h3 className="mt-2 text-xl font-bold text-white">{order.customer.fullName}</h3>
                <p className="mt-1 text-sm text-slate-400">{order.customer.email}</p>
              </div>

              <div className="flex flex-wrap gap-2">
                <span className={`badge ${statusClass(order.orderStatus)}`}>{order.orderStatus}</span>
                <span className={`badge ${statusClass(order.paymentStatus)}`}>{order.paymentStatus}</span>
                <span className={`badge ${statusClass(order.shipmentStatus)}`}>{order.shipmentStatus}</span>
              </div>
            </div>

            <div className="mt-5 overflow-x-auto">
              <table className="w-full min-w-[700px] text-left text-sm">
                <thead className="text-xs uppercase tracking-wider text-slate-500">
                  <tr>
                    <th className="py-3">SKU</th>
                    <th className="py-3">Product</th>
                    <th className="py-3">Warehouse</th>
                    <th className="py-3">Qty</th>
                    <th className="py-3">Line Total</th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map((item) => (
                    <tr key={item.id} className="border-t border-slate-800">
                      <td className="py-3 font-mono text-cyan-300">{item.sku}</td>
                      <td className="py-3 text-white">{item.productName}</td>
                      <td className="py-3 text-slate-300">{item.warehouseCode}</td>
                      <td className="py-3 text-slate-300">{item.quantity}</td>
                      <td className="py-3 text-white">${item.lineTotal}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="mt-5 flex flex-col justify-between gap-4 md:flex-row md:items-center">
              <p className="text-lg font-bold text-white">Total: ${order.totalAmount}</p>

              <div className="flex flex-wrap gap-3">
                <button
                  onClick={() => runOrderAction(() => markOrderPaid(order.id), "Order marked as paid.")}
                  className="btn-secondary"
                >
                  Mark Paid
                </button>

                <button
                  onClick={() => runOrderAction(() => shipOrder(order.id), "Order shipped and stock deducted.")}
                  className="btn-secondary"
                >
                  Ship
                </button>

                <button
                  onClick={() => runOrderAction(() => cancelOrder(order.id), "Order cancelled and reservation released.")}
                  className="rounded-xl border border-red-500/40 px-4 py-3 text-sm font-semibold text-red-300 hover:bg-red-500/10"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        ))}

        {orders.length === 0 && <div className="card p-8 text-center text-slate-400">No orders found.</div>}
      </div>
    </div>
  );
}
