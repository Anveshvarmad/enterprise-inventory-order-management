import { FormEvent, useEffect, useState } from "react";
import {
  createProduct,
  deleteProduct,
  getCategories,
  getProducts,
  getSuppliers
} from "../lib/api";
import type { Category, Product, Supplier } from "../types";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [sku, setSku] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [supplierId, setSupplierId] = useState("");
  const [unitPrice, setUnitPrice] = useState("");
  const [reorderLevel, setReorderLevel] = useState("");

  async function loadData() {
    const [productData, categoryData, supplierData] = await Promise.all([
      getProducts(),
      getCategories(),
      getSuppliers()
    ]);

    setProducts(productData.content);
    setCategories(categoryData);
    setSuppliers(supplierData);

    if (!categoryId && categoryData.length > 0) {
      setCategoryId(String(categoryData[0].id));
    }

    if (!supplierId && supplierData.length > 0) {
      setSupplierId(String(supplierData[0].id));
    }
  }

  useEffect(() => {
    loadData().catch((err) => setError(err instanceof Error ? err.message : "Failed to load products"));
  }, []);

  async function handleCreateProduct(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      await createProduct({
        sku,
        name,
        description,
        categoryId: Number(categoryId),
        supplierId: Number(supplierId),
        unitPrice: Number(unitPrice),
        reorderLevel: Number(reorderLevel)
      });

      setSku("");
      setName("");
      setDescription("");
      setUnitPrice("");
      setReorderLevel("");
      setMessage("Product created successfully.");
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create product");
    }
  }

  async function handleDeleteProduct(id: number) {
    setError("");
    setMessage("");

    try {
      await deleteProduct(id);
      setMessage("Product discontinued successfully.");
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete product");
    }
  }

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">Catalog</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Products</h2>
        <p className="mt-2 text-slate-400">Create, view, and discontinue product records.</p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}
      {message && <div className="mb-6 rounded-xl bg-emerald-500/10 p-4 text-emerald-300">{message}</div>}

      <form onSubmit={handleCreateProduct} className="card mb-8 grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-4">
        <input className="input" placeholder="SKU" value={sku} onChange={(e) => setSku(e.target.value)} required />
        <input className="input" placeholder="Product name" value={name} onChange={(e) => setName(e.target.value)} required />
        <input className="input" placeholder="Description" value={description} onChange={(e) => setDescription(e.target.value)} />

        <select className="input" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>

        <select className="input" value={supplierId} onChange={(e) => setSupplierId(e.target.value)} required>
          {suppliers.map((supplier) => (
            <option key={supplier.id} value={supplier.id}>
              {supplier.name}
            </option>
          ))}
        </select>

        <input className="input" type="number" step="0.01" placeholder="Unit price" value={unitPrice} onChange={(e) => setUnitPrice(e.target.value)} required />
        <input className="input" type="number" placeholder="Reorder level" value={reorderLevel} onChange={(e) => setReorderLevel(e.target.value)} required />

        <button className="btn-primary">Create Product</button>
      </form>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left text-sm">
            <thead className="border-b border-slate-800 bg-slate-950/70 text-xs uppercase tracking-wider text-slate-400">
              <tr>
                <th className="px-5 py-4">SKU</th>
                <th className="px-5 py-4">Product</th>
                <th className="px-5 py-4">Category</th>
                <th className="px-5 py-4">Supplier</th>
                <th className="px-5 py-4">Price</th>
                <th className="px-5 py-4">Reorder</th>
                <th className="px-5 py-4">Status</th>
                <th className="px-5 py-4">Action</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-b border-slate-800/70 hover:bg-slate-900">
                  <td className="px-5 py-4 font-mono text-cyan-300">{product.sku}</td>
                  <td className="px-5 py-4">
                    <p className="font-semibold text-white">{product.name}</p>
                    <p className="mt-1 text-xs text-slate-500">{product.description}</p>
                  </td>
                  <td className="px-5 py-4 text-slate-300">{product.category?.name || "-"}</td>
                  <td className="px-5 py-4 text-slate-300">{product.supplier?.name || "-"}</td>
                  <td className="px-5 py-4 text-slate-100">${product.unitPrice}</td>
                  <td className="px-5 py-4 text-slate-300">{product.reorderLevel}</td>
                  <td className="px-5 py-4">
                    <span className="badge bg-emerald-500/10 text-emerald-300">{product.status}</span>
                  </td>
                  <td className="px-5 py-4">
                    <button
                      onClick={() => handleDeleteProduct(product.id)}
                      className="rounded-lg border border-red-500/40 px-3 py-2 text-xs font-semibold text-red-300 hover:bg-red-500/10"
                    >
                      Discontinue
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {products.length === 0 && <div className="p-8 text-center text-slate-400">No products found.</div>}
        </div>
      </div>
    </div>
  );
}
