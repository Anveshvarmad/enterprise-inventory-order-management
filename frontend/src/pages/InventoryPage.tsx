import { FormEvent, useEffect, useState } from "react";
import { adjustStock, getInventory, getProducts, getWarehouses } from "../lib/api";
import type { InventoryItem, Product, Warehouse } from "../types";

export default function InventoryPage() {
  const [inventory, setInventory] = useState<InventoryItem[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [productId, setProductId] = useState("");
  const [warehouseId, setWarehouseId] = useState("");
  const [quantityChange, setQuantityChange] = useState("");
  const [notes, setNotes] = useState("");

  async function loadData() {
    const [inventoryData, productData, warehouseData] = await Promise.all([
      getInventory(),
      getProducts(),
      getWarehouses()
    ]);

    setInventory(inventoryData.content);
    setProducts(productData.content);
    setWarehouses(warehouseData);

    if (!productId && productData.content.length > 0) {
      setProductId(String(productData.content[0].id));
    }

    if (!warehouseId && warehouseData.length > 0) {
      setWarehouseId(String(warehouseData[0].id));
    }
  }

  useEffect(() => {
    loadData().catch((err) => setError(err instanceof Error ? err.message : "Failed to load inventory"));
  }, []);

  async function handleAdjustStock(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      await adjustStock({
        productId: Number(productId),
        warehouseId: Number(warehouseId),
        quantityChange: Number(quantityChange),
        notes
      });

      setQuantityChange("");
      setNotes("");
      setMessage("Inventory adjusted successfully.");
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to adjust inventory");
    }
  }

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Warehouses</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Inventory</h2>
        <p className="mt-2 text-slate-400">Adjust stock and monitor available quantity across warehouses.</p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}
      {message && <div className="mb-6 rounded-xl bg-emerald-500/10 p-4 text-emerald-300">{message}</div>}

      <form onSubmit={handleAdjustStock} className="card mb-8 grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-5">
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
              {warehouse.code} - {warehouse.name}
            </option>
          ))}
        </select>

        <input
          className="input"
          type="number"
          placeholder="Quantity change, ex: 20 or -5"
          value={quantityChange}
          onChange={(e) => setQuantityChange(e.target.value)}
          required
        />

        <input className="input" placeholder="Notes" value={notes} onChange={(e) => setNotes(e.target.value)} />

        <button className="btn-primary">Adjust Stock</button>
      </form>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left text-sm">
            <thead className="border-b border-slate-800 bg-slate-950/70 text-xs uppercase tracking-wider text-slate-400">
              <tr>
                <th className="px-5 py-4">Product</th>
                <th className="px-5 py-4">Warehouse</th>
                <th className="px-5 py-4">On Hand</th>
                <th className="px-5 py-4">Reserved</th>
                <th className="px-5 py-4">Available</th>
                <th className="px-5 py-4">Reorder Level</th>
                <th className="px-5 py-4">Status</th>
              </tr>
            </thead>
            <tbody>
              {inventory.map((item) => (
                <tr key={item.id} className="border-b border-slate-800/70 hover:bg-slate-900">
                  <td className="px-5 py-4">
                    <p className="font-semibold text-white">{item.productName}</p>
                    <p className="mt-1 font-mono text-xs text-cyan-300">{item.sku}</p>
                  </td>
                  <td className="px-5 py-4">
                    <p className="text-slate-200">{item.warehouseName}</p>
                    <p className="mt-1 text-xs text-slate-500">{item.warehouseCode}</p>
                  </td>
                  <td className="px-5 py-4 text-slate-100">{item.quantityOnHand}</td>
                  <td className="px-5 py-4 text-slate-300">{item.reservedQuantity}</td>
                  <td className="px-5 py-4 font-semibold text-white">{item.availableQuantity}</td>
                  <td className="px-5 py-4 text-slate-300">{item.reorderLevel}</td>
                  <td className="px-5 py-4">
                    {item.lowStock ? (
                      <span className="badge bg-red-500/10 text-red-300">LOW STOCK</span>
                    ) : (
                      <span className="badge bg-emerald-500/10 text-emerald-300">HEALTHY</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {inventory.length === 0 && <div className="p-8 text-center text-slate-400">No inventory found.</div>}
        </div>
      </div>
    </div>
  );
}
