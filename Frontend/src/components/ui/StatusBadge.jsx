function badgeTone(value) {
  const normalized = String(value).toLowerCase();
  if (normalized.includes("deliver") || normalized.includes("stock")) return "success";
  if (normalized.includes("low") || normalized.includes("pack") || normalized.includes("transit")) return "warning";
  return "danger";
}

export function StatusBadge({ children }) {
  return <span className={`status ${badgeTone(children)}`}>{children}</span>;
}
