import type { LucideIcon } from "lucide-react";

type Props = {
  title: string;
  value: string | number;
  icon: LucideIcon;
  subtitle?: string;
};

export default function StatCard({ title, value, icon: Icon, subtitle }: Props) {
  return (
    <div className="card p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">{title}</p>
          <h3 className="mt-3 text-3xl font-bold text-white">{value}</h3>
          {subtitle && <p className="mt-2 text-xs text-slate-500">{subtitle}</p>}
        </div>

        <div className="rounded-2xl bg-slate-800 p-3 text-cyan-300">
          <Icon size={24} />
        </div>
      </div>
    </div>
  );
}
