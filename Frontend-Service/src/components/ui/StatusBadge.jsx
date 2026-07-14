function badgeTone(value) {
  const normalized = String(value).toLowerCase();
  if (normalized.includes("low") || normalized.includes("pack") || normalized.includes("transit")) return "warning";
  if (normalized.includes("deliver") || normalized.includes("stock")) return "success";
  return "danger";
}

export function StatusBadge({ children, variant }) {
  return <span className={`status ${variant ?? badgeTone(children)}`}>{children}</span>;
}
