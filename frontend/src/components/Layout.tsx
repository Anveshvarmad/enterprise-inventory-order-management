import {
  BarChart3,
  Boxes,
  BrainCircuit,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  PackageSearch,
  Warehouse
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { getCurrentUser, logout } from "../lib/auth";

const navItems = [
  { path: "/", label: "Dashboard", icon: LayoutDashboard },
  { path: "/products", label: "Products", icon: PackageSearch },
  { path: "/inventory", label: "Inventory", icon: Warehouse },
  { path: "/orders", label: "Orders", icon: ClipboardList },
  { path: "/analytics", label: "Analytics", icon: BarChart3 },
  { path: "/forecast", label: "ML Forecast", icon: BrainCircuit }
];

export default function Layout() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="min-h-screen bg-slate-950">
      <aside className="fixed left-0 top-0 hidden h-full w-72 border-r border-slate-800 bg-slate-950/90 p-6 lg:block">
        <div className="mb-10 flex items-center gap-3">
          <div className="rounded-2xl bg-cyan-400 p-3 text-slate-950">
            <Boxes size={26} />
          </div>
          <div>
            <h1 className="text-lg font-bold text-white">InventoryOps</h1>
            <p className="text-xs text-slate-400">Enterprise Control Panel</p>
          </div>
        </div>

        <nav className="space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === "/"}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition ${
                    isActive
                      ? "bg-cyan-400 text-slate-950"
                      : "text-slate-300 hover:bg-slate-900 hover:text-white"
                  }`
                }
              >
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="absolute bottom-6 left-6 right-6">
          <div className="mb-4 rounded-2xl border border-slate-800 bg-slate-900 p-4">
            <p className="text-sm font-semibold text-white">{user?.fullName}</p>
            <p className="mt-1 text-xs text-slate-400">{user?.email}</p>
            <p className="mt-2 inline-flex rounded-full bg-slate-800 px-3 py-1 text-xs text-cyan-300">
              {user?.role}
            </p>
          </div>

          <button onClick={handleLogout} className="btn-secondary flex w-full items-center justify-center gap-2">
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </aside>

      <main className="lg:pl-72">
        <div className="mx-auto max-w-7xl px-5 py-6 lg:px-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
