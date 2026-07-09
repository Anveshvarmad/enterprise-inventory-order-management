import { Boxes } from "lucide-react";
import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../lib/api";
import { saveAuth } from "../lib/auth";

export default function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("admin@inventory.com");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const auth = await login(email, password);
      saveAuth(auth);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,#164e63,#020617_45%)] px-5">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-3xl bg-cyan-400 text-slate-950">
            <Boxes size={34} />
          </div>
          <h1 className="text-3xl font-bold text-white">Enterprise Inventory</h1>
          <p className="mt-2 text-sm text-slate-400">Inventory, orders, warehouses, and operations dashboard</p>
        </div>

        <form onSubmit={handleSubmit} className="card space-y-5 p-6">
          <div>
            <label className="mb-2 block text-sm text-slate-300">Email</label>
            <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-300">Password</label>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {error && (
            <div className="rounded-xl border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-300">
              {error}
            </div>
          )}

          <button disabled={loading} className="btn-primary w-full">
            {loading ? "Signing in..." : "Sign in"}
          </button>

          <div className="rounded-xl bg-slate-950 p-4 text-xs text-slate-400">
            <p className="font-semibold text-slate-300">Demo users</p>
            <p className="mt-2">admin@inventory.com / admin123</p>
            <p>manager@inventory.com / manager123</p>
            <p>warehouse@inventory.com / staff123</p>
            <p>sales@inventory.com / sales123</p>
            <p>support@inventory.com / support123</p>
          </div>
        </form>
      </div>
    </div>
  );
}
