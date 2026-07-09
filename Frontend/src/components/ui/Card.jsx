export function Card({ children, className = "", padded = true, ...props }) {
  return (
    <div className={`card ${padded ? "card-pad" : ""} ${className}`.trim()} {...props}>
      {children}
    </div>
  );
}
