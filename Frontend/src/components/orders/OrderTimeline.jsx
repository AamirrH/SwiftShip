import { CheckCircle2, Home, Package, ReceiptText, Truck } from "lucide-react";

const icons = [ReceiptText, Package, Truck, Home];

export function OrderTimeline({ steps }) {
  return (
    <div className="stepper">
      {steps.map((step, index) => {
        const Icon = step.state === "done" ? CheckCircle2 : icons[index];
        return (
          <div className={`step ${step.state === "done" ? "done" : ""} ${step.state === "active" ? "active" : ""}`} key={step.label}>
            <div className="step-icon">
              <Icon size={20} />
            </div>
            <div>
              <strong>{step.label}</strong>
              <div className="label-caps" style={{ marginTop: 4 }}>{step.time}</div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
