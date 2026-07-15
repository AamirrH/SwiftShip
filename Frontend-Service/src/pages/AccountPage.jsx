import { Bell, Home, MapPinned, UserRound } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";

const emptyAddress = {
  label: "Home",
  addressLine: "",
  city: "",
  state: "",
  pincode: "",
  defaultAddress: true,
};

export function AccountPage({ authUser }) {
  const { data: loadedAddresses, status: addressStatus } = useApiResource(
    () => (authUser ? api.getMyCustomerAddresses() : Promise.resolve([])),
    [],
    [authUser?.email, authUser?.username]
  );
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState("new");
  const [addressForm, setAddressForm] = useState(emptyAddress);
  const [saveStatus, setSaveStatus] = useState("idle");
  const [saveMessage, setSaveMessage] = useState("");

  const sortedAddresses = useMemo(
    () => [...addresses].sort((a, b) => Number(Boolean(b.defaultAddress)) - Number(Boolean(a.defaultAddress))),
    [addresses]
  );
  const defaultAddress = sortedAddresses.find((address) => address.defaultAddress) ?? sortedAddresses[0];
  const displayName = authUser?.username ?? "Aamir Customer";
  const displayEmail = authUser?.email ?? "Signed in customer";

  useEffect(() => {
    setAddresses(loadedAddresses);
    const nextAddress = loadedAddresses.find((address) => address.defaultAddress) ?? loadedAddresses[0] ?? emptyAddress;
    setSelectedAddressId(nextAddress.id ?? "new");
    setAddressForm(nextAddress);
  }, [loadedAddresses]);

  function selectAddress(addressId) {
    if (addressId === "new") {
      setSelectedAddressId("new");
      setAddressForm(emptyAddress);
      return;
    }

    const nextAddress = addresses.find((address) => String(address.id) === addressId);
    if (!nextAddress) return;
    setSelectedAddressId(nextAddress.id);
    setAddressForm(nextAddress);
  }

  function updateAddressField(field, value) {
    setAddressForm((current) => ({ ...current, [field]: value }));
  }

  async function saveAddress(event) {
    event.preventDefault();
    setSaveStatus("saving");
    setSaveMessage("");

    const payload = {
      label: addressForm.label,
      addressLine: addressForm.addressLine,
      city: addressForm.city,
      state: addressForm.state,
      pincode: addressForm.pincode,
      defaultAddress: Boolean(addressForm.defaultAddress),
    };

    try {
      const savedAddress =
        selectedAddressId === "new"
          ? await api.createMyCustomerAddress(payload)
          : await api.updateMyCustomerAddress(selectedAddressId, payload);

      setAddresses((current) => upsertAddress(current, savedAddress));
      setSelectedAddressId(savedAddress.id);
      setAddressForm(savedAddress);
      setSaveStatus("saved");
      setSaveMessage("Address saved.");
    } catch (error) {
      setSaveStatus("error");
      setSaveMessage("We could not save this address right now. Please try again.");
    }
  }

  return (
    <section className="page account-page">
      <form className="account-form" onSubmit={saveAddress}>
        <div className="page-header">
          <div>
            <span className="label-caps">Customer profile</span>
            <h1 className="page-title">Account settings</h1>
          </div>
          <Button disabled={saveStatus === "saving"} type="submit">
            {saveStatus === "saving" ? "Saving..." : "Save changes"}
          </Button>
        </div>

        <div className="account-overview">
          <Card className="account-profile-card">
            <div style={{ display: "flex", gap: 16, marginBottom: 22 }}>
              <div className="brand-mark"><UserRound size={22} /></div>
              <div>
                <h2 className="section-title" style={{ margin: 0 }}>{displayName}</h2>
                <p className="muted" style={{ margin: "4px 0 0" }}>{displayEmail}</p>
              </div>
            </div>
            <div className="grid two">
              <Field label="Full name" value={displayName} />
              <Field label="Phone" value="+91 98765 43210" />
              <Field label="Email" value={displayEmail} />
              <Field label="Membership" value="SwiftShip customer" />
            </div>
          </Card>

          <div className="account-preferences">
            <Preference icon={Home} label="Default address" value={formatAddress(defaultAddress)} />
            <Preference icon={MapPinned} label="Saved locations" value={savedLocationLabels(sortedAddresses)} />
            <Preference icon={Bell} label="Notifications" value="Order, ETA, delivered" />
          </div>
        </div>

        <Card className="account-address-card">
          <div className="account-card-header">
            <div>
              <span className="label-caps">Delivery address</span>
              <h2 className="section-title" style={{ marginTop: 6 }}>Change saved address</h2>
            </div>
            {addressStatus === "fallback" && (
              <span className="muted">Your saved addresses are taking a moment to load.</span>
            )}
          </div>

          <label>
            <span className="label-caps">Choose address</span>
            <select
              className="input"
              onChange={(event) => selectAddress(event.target.value)}
              style={{ marginTop: 8 }}
              value={String(selectedAddressId)}
            >
              {sortedAddresses.map((address) => (
                <option key={address.id} value={address.id}>
                  {address.label || "Saved address"}
                </option>
              ))}
              <option value="new">Add new address</option>
            </select>
          </label>

          <div className="grid two" style={{ gap: 12, marginTop: 14 }}>
            <EditableField label="Label" onChange={(value) => updateAddressField("label", value)} value={addressForm.label} />
            <EditableField label="Pincode" onChange={(value) => updateAddressField("pincode", value)} value={addressForm.pincode} />
            <EditableField label="City" onChange={(value) => updateAddressField("city", value)} value={addressForm.city} />
            <EditableField label="State" onChange={(value) => updateAddressField("state", value)} value={addressForm.state} />
          </div>

          <label style={{ display: "block", marginTop: 12 }}>
            <span className="label-caps">Address line</span>
            <textarea
              className="input"
              onChange={(event) => updateAddressField("addressLine", event.target.value)}
              required
              rows="3"
              style={{ marginTop: 8, paddingBottom: 10, paddingTop: 10, resize: "vertical" }}
              value={addressForm.addressLine}
            />
          </label>

          <label style={{ alignItems: "center", display: "flex", gap: 10, marginTop: 14 }}>
            <input
              checked={Boolean(addressForm.defaultAddress)}
              onChange={(event) => updateAddressField("defaultAddress", event.target.checked)}
              type="checkbox"
            />
            <span className="muted">Use this as my default delivery address</span>
          </label>

          {saveMessage && (
            <p className={saveStatus === "error" ? "" : "muted"} style={{ marginBottom: 0 }}>
              {saveMessage}
            </p>
          )}
        </Card>
      </form>
    </section>
  );
}

function upsertAddress(addresses, savedAddress) {
  const nextAddresses = addresses.filter((address) => address.id !== savedAddress.id);
  if (savedAddress.defaultAddress) {
    return [{ ...savedAddress }, ...nextAddresses.map((address) => ({ ...address, defaultAddress: false }))];
  }
  return [{ ...savedAddress }, ...nextAddresses];
}

function savedLocationLabels(addresses) {
  const labels = addresses.map((address) => address.label).filter(Boolean);
  return labels.length ? labels.join(", ") : "No saved locations";
}

function formatAddress(address) {
  if (!address) return "No default address";
  return [address.addressLine, address.city, address.state, address.pincode].filter(Boolean).join(", ");
}

function Field({ label, value }) {
  return (
    <label>
      <span className="label-caps">{label}</span>
      <input className="input" defaultValue={value} readOnly style={{ marginTop: 8 }} />
    </label>
  );
}

function EditableField({ label, onChange, value }) {
  return (
    <label>
      <span className="label-caps">{label}</span>
      <input className="input" onChange={(event) => onChange(event.target.value)} required style={{ marginTop: 8 }} value={value ?? ""} />
    </label>
  );
}

function Preference({ icon: Icon, label, value }) {
  return (
    <Card>
      <Icon size={22} style={{ color: "var(--primary)" }} />
      <div className="label-caps" style={{ marginTop: 12 }}>{label}</div>
      <strong>{value}</strong>
    </Card>
  );
}
