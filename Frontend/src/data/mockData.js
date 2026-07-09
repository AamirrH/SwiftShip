export const productImages = {
  tablet:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuDecVlNjohezceilasE5AnSH6g-G50iD2D8FMtwqtPUmqR-DpY0tF7RxN3LJJzTTiXbiuWx9luGlBy8q6td7fi8vV-eezupD6run7K7D-sMkVD5fDMrp_nE_j7YVdpIlbZQA9kUwwElyhGQEvYMuFbmCIboGTn_fPpy0Hea_30gV2qzSsiWq1N7ONM557CQpbBbRcK_v9Y5JgLbhtDe2mOBDo92f4BXoWbg6SshIt47XROGOmefPOhzee_SF2-50j7TBVEnTCPKCo0",
  straps:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAHD7LzXC5AGRGSR44YTSAtErN7jsVlyZbcbtEzvuVqeLX8roESrQNJPWPE3a2xL_bqP5XNr3XMskd1NX5cTHYygBS2AfIhUurglPMhsNr2saEPVZzgH_UBbkWD3AXAcM0HxPZj60KwMc5O9h0_v4E8_NV1rCvWX80nyuLT_M1W_apjtm6aeSaiMd6HtEfzDDTx79SrWH77ch0Y30_cT0sNCamzjXiODbOsVDyebHjeA4er_3vd5d9Gzlbrg-KfaWG1mW8N2r60nIc",
  labels:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBfelpRdsX9YYg3NOjnZzlFz0pv23Gr2UaCUrDibSa4wHImmDnEsyIcDAPeT5oyaS0y5J_O0I4StE6QkJLzp4tDdrtUZualaWmR3ejEsfraBGFVyvwLLU6Jhi9CoWRaTIb_NxDzr05-otjzW0sk2W7P7r49bWV2rodnL0775R3jXRN8k4eRC4jdJfMqXTVZy1EAzpWjghpKV-dAjYcs6niesPggSaVBTDcKz1bTNi0rDeZ8QpMN2Vxjcep2eEFOTljOW5OnW-xUjMA",
  boxes:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBZfyOzfogocnmfXuuRJF884in5zQWYxexGx1chOXQhE740AzS8jJ6wHwkcuJcBHDbzhmj08SmebTEoRQ5xlyEoo0MC42V6aLCEGCJOaegIItbR46osNKO1T-ZlHWXe4VOXHI4whq6-FmHXxZ0xtjFfFxv6tEG1lvYnHSQ31aJ3_xqir4zH7XrWo5_rXwU2tq33ggHupuS2lPorlpiT656boxUSQFQNpuyMfcKfI5CMC7ACeDHYyexinhMBPlkniT6oVWQpV7-qokI",
  tape:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAMZR07vhi6pFDcQ2_GQfMtrBgVqouEaP3N1kS1r66aZnjZETz7oVMPp9dnIRj-BzFHmk0oLO9Az-1e4Gh5-q_B3Dq9vdBptvN0zZ1Qlb0kGrS4XF5sOfKrKykviIWzkqRjoO-q_4wheYT3lv-aG7FnYHUakzUHmY4q-uk4J96aBE4qp3sc_iCW3eVR7oqTs0AVxULZUsFCI-vf7cPGOmbhK7QEJI9jZYVVxDKOPWHcCCvDo4sC-2ItPGBd-gQ8P9gJqmo3z3swPQs",
  scale:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAhsakcCaAGEbDk0x1pkI6MofWT4WxLuyVG0tPXYTnGO6LnYbBssdJ5XZ_8hafudof0VELq-zT3pGMSZa2LVpSryxWeC_KJ2d2gmlT-Y84gbvpZIDS1pff1zYM8BIlHzXG8tLMIIbmBQi31TuL3IKnNFx6tsQCMCRE_XWrKmr9kO1oNy1-KmgDFDFTxKw0Cf9d0lIR8yj80dXUiKpjm9yFdIzSbdA5Ds7hNcB3cQZ7Eo6yu92QDtcIeo1faKHVPE8Aa0cXHJVv54oI",
};

export const mockProducts = [
  {
    id: 1,
    productName: "ProTracker XT-900 Tablet",
    productPrice: 45999,
    stock: 18,
    category: "Electronics",
    status: "In stock",
    image: productImages.tablet,
    description:
      "Rugged logistics tablet with barcode scanning, 5G support, live order lookup, and warehouse-ready casing.",
  },
  {
    id: 2,
    productName: "Titan Grip Ratchet Straps",
    productPrice: 3250,
    stock: 7,
    category: "Logistics Gear",
    status: "Low stock",
    image: productImages.straps,
    description: "Industrial 5-ton cargo straps for high-value shipments and secure warehouse transfer.",
  },
  {
    id: 3,
    productName: "ThermalWay Shipping Labels",
    productPrice: 850,
    stock: 120,
    category: "Packaging",
    status: "In stock",
    image: productImages.labels,
    description: "Smudge-proof thermal labels for fast fulfillment lanes, courier handoff, and returns.",
  },
  {
    id: 4,
    productName: "Heavy-Duty Pallet Boxes",
    productPrice: 2450,
    stock: 42,
    category: "Packaging",
    status: "In stock",
    image: productImages.boxes,
    description: "Stackable corrugated boxes designed for warehouse staging and last-mile protection.",
  },
  {
    id: 5,
    productName: "High-Tensile Security Tape",
    productPrice: 850,
    stock: 32,
    category: "Packaging",
    status: "In stock",
    image: productImages.tape,
    description: "Tamper-evident reinforced tape for fragile parcels and high-value item dispatch.",
  },
  {
    id: 6,
    productName: "Precision Cargo Scale",
    productPrice: 14200,
    stock: 4,
    category: "Logistics Gear",
    status: "Low stock",
    image: productImages.scale,
    description: "Bluetooth-enabled scale for accurate freight pricing and inventory verification.",
  },
];

export const mockCart = [
  {
    id: "cart-1",
    productId: 4,
    name: "Heavy-Duty Pallet Boxes",
    price: 2450,
    image: productImages.boxes,
    quantity: 2,
  },
  {
    id: "cart-2",
    productId: 3,
    name: "ThermalWay Shipping Labels",
    price: 850,
    image: productImages.labels,
    quantity: 4,
  },
];

export const mockOrders = [
  {
    id: 8829,
    number: "ORD-8829-XL",
    status: "In transit",
    eta: "28 min",
    placedAt: "Today, 14:32",
    total: 11502,
    destination: "Koramangala, Bengaluru",
    itemCount: 3,
  },
  {
    id: 7721,
    number: "ORD-7721-QA",
    status: "Packed",
    eta: "2 hr",
    placedAt: "Yesterday, 19:08",
    total: 5120,
    destination: "Indiranagar, Bengaluru",
    itemCount: 2,
  },
  {
    id: 6244,
    number: "ORD-6244-MN",
    status: "Delivered",
    eta: "Done",
    placedAt: "Jul 08, 10:18",
    total: 8900,
    destination: "HSR Layout, Bengaluru",
    itemCount: 1,
  },
];

export const mockTracking = {
  orderNumber: 8829,
  driverName: "Arjun Mehta",
  status: "IN_TRANSIT",
  currentEtaMinutes: 28,
  totalDistanceKm: 12.8,
  remainingDistanceKm: 4.1,
  customerAddress: "18th Main Road, Koramangala, Bengaluru",
  currentLocation: "Near Sony World Signal",
  steps: [
    { label: "Ordered", time: "14:32", state: "done" },
    { label: "Reserved", time: "14:34", state: "done" },
    { label: "On Route", time: "14:48", state: "active" },
    { label: "Delivered", time: "Pending", state: "todo" },
  ],
};
