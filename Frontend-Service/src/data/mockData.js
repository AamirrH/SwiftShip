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
  headphones:
    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
  smartwatch:
    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80",
  groceries:
    "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=80",
  skincare:
    "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=80",
  backpack:
    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80",
  lamp:
    "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80",
};

export const mockProducts = [
  {
    id: 1,
    productName: "AeroBass Wireless Headphones",
    productPrice: 4999,
    stock: 18,
    category: "Electronics",
    status: "In stock",
    image: productImages.headphones,
    description:
      "Noise-isolating headphones with deep bass, quick charging, and a foldable everyday carry design.",
  },
  {
    id: 2,
    productName: "PulseFit Smart Watch",
    productPrice: 7999,
    stock: 7,
    category: "Electronics",
    status: "Low stock",
    image: productImages.smartwatch,
    description: "Daily health tracking, notifications, water resistance, and a clean AMOLED display.",
  },
  {
    id: 3,
    productName: "Fresh Basket Grocery Kit",
    productPrice: 1299,
    stock: 120,
    category: "Daily Essentials",
    status: "In stock",
    image: productImages.groceries,
    description: "A quick-replenish set of fruits, pantry staples, breakfast items, and household basics.",
  },
  {
    id: 4,
    productName: "GlowCare Skincare Set",
    productPrice: 1899,
    stock: 42,
    category: "Personal Care",
    status: "In stock",
    image: productImages.skincare,
    description: "Cleanser, serum, and moisturizer set for a simple daily self-care routine.",
  },
  {
    id: 5,
    productName: "UrbanTrail Day Backpack",
    productPrice: 2499,
    stock: 32,
    category: "Lifestyle",
    status: "In stock",
    image: productImages.backpack,
    description: "Water-resistant backpack with laptop storage, quick pockets, and commuter comfort.",
  },
  {
    id: 6,
    productName: "LumaDesk Study Lamp",
    productPrice: 1599,
    stock: 4,
    category: "Home",
    status: "Low stock",
    image: productImages.lamp,
    description: "Adjustable warm-to-cool desk lamp with a compact base and low power draw.",
  },
  {
    id: 7,
    productName: "Heavy-Duty Pallet Boxes",
    productPrice: 2450,
    stock: 42,
    category: "Packaging",
    status: "In stock",
    image: productImages.boxes,
    description: "Stackable corrugated boxes for customers sending fragile or bulk items.",
  },
  {
    id: 8,
    productName: "Precision Cargo Scale",
    productPrice: 14200,
    stock: 4,
    category: "Logistics Gear",
    status: "Low stock",
    image: productImages.scale,
    description: "Bluetooth-enabled scale for accurate freight pricing and package preparation.",
  },
];

export const mockCart = [
  {
    id: "cart-1",
    productId: 3,
    name: "Fresh Basket Grocery Kit",
    price: 1299,
    image: productImages.groceries,
    quantity: 1,
  },
  {
    id: "cart-2",
    productId: 1,
    name: "AeroBass Wireless Headphones",
    price: 4999,
    image: productImages.headphones,
    quantity: 1,
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

export const mockNotifications = [
  {
    notificationId: 101,
    customerId: 1,
    orderNumber: 8829,
    notificationType: "ETA_UPDATED",
    notificationChannel: "IN_APP",
    title: "Your order is 28 minutes away",
    message: "Arjun is near Sony World Signal with your Fresh Basket and headphones.",
    readStatus: "UNREAD",
    deliveryStatus: "NOT_REQUIRED",
    createdAt: "2026-07-11T09:45:00Z",
  },
  {
    notificationId: 102,
    customerId: 1,
    orderNumber: 8829,
    notificationType: "ROUTE_CALCULATED",
    notificationChannel: "IN_APP",
    title: "Fastest route selected",
    message: "SwiftShip picked the Koramangala route after warehouse assignment.",
    readStatus: "UNREAD",
    deliveryStatus: "NOT_REQUIRED",
    createdAt: "2026-07-11T09:26:00Z",
  },
  {
    notificationId: 103,
    customerId: 1,
    orderNumber: 7721,
    notificationType: "ORDER_CONFIRMED",
    notificationChannel: "IN_APP",
    title: "Order confirmed",
    message: "Inventory has reserved your items and packing has started.",
    readStatus: "READ",
    deliveryStatus: "SENT",
    createdAt: "2026-07-10T13:20:00Z",
    readAt: "2026-07-10T13:42:00Z",
  },
  {
    notificationId: 104,
    customerId: 1,
    orderNumber: 6244,
    notificationType: "ORDER_DELIVERED",
    notificationChannel: "IN_APP",
    title: "Delivered successfully",
    message: "Your order was delivered to HSR Layout. Thanks for using SwiftShip.",
    readStatus: "READ",
    deliveryStatus: "SENT",
    createdAt: "2026-07-08T11:08:00Z",
    readAt: "2026-07-08T11:12:00Z",
  },
];

export const mockWarehouses = [
  {
    id: "8a39c2a7-73de-4c63-b4a1-b8f3b67a91b1",
    warehouseName: "Koramangala Rapid Hub",
    city: "Bengaluru",
    lat: 12.9352,
    lng: 77.6245,
    capacity: 9200,
    active: true,
  },
  {
    id: "edb5ac32-e283-4187-bf70-44935038c0ec",
    warehouseName: "Indiranagar Dark Store",
    city: "Bengaluru",
    lat: 12.9784,
    lng: 77.6408,
    capacity: 5400,
    active: true,
  },
  {
    id: "bf96dfbd-a9d7-4c8b-8429-48051d31c909",
    warehouseName: "HSR Reserve Facility",
    city: "Bengaluru",
    lat: 12.9116,
    lng: 77.6389,
    capacity: 7800,
    active: false,
  },
  {
    id: "776fdd44-a43e-489b-a04c-05c96bd3fc19",
    warehouseName: "Whitefield Express Node",
    city: "Bengaluru",
    lat: 12.9698,
    lng: 77.75,
    capacity: 11800,
    active: true,
  },
];

export const mockRouteOptions = [
  {
    routeId: 1,
    totalDistance: 7.8,
    timeToReach: 28,
  },
  {
    routeId: 2,
    totalDistance: 9.4,
    timeToReach: 24,
  },
  {
    routeId: 3,
    totalDistance: 6.9,
    timeToReach: 34,
  },
];

export const mockSelectedRoute = {
  routeId: 1,
  totalDistance: 7.8,
  timeToReach: 28,
  reasoning: "Balanced distance and ETA with fewer congestion points near the destination.",
};

export const mockSelectedRouteRecords = [
  {
    serialId: "fe6dbd98-94f5-43be-bfd9-c41cc1e59ad2",
    selectedRouteId: 1,
    customerId: 1,
    orderId: 8829,
    warehouseId: mockWarehouses[0].id,
    customerAddress: "18th Main Road, Koramangala, Bengaluru",
    customerLat: 12.9716,
    customerLng: 77.5946,
    warehouseLat: mockWarehouses[0].lat,
    warehouseLng: mockWarehouses[0].lng,
    totalDistance: 7.8,
    timeToReach: 28,
    reasoning: "Balanced distance and ETA with fewer congestion points near the destination.",
  },
  {
    serialId: "6774fd23-8a47-4878-b6e2-33143cc746c2",
    selectedRouteId: 2,
    customerId: 1,
    orderId: 7721,
    warehouseId: mockWarehouses[1].id,
    customerAddress: "Indiranagar 100 Feet Road, Bengaluru",
    customerLat: 12.9784,
    customerLng: 77.6408,
    warehouseLat: mockWarehouses[1].lat,
    warehouseLng: mockWarehouses[1].lng,
    totalDistance: 5.6,
    timeToReach: 21,
    reasoning: "Lower ETA from the assigned dark store with a direct arterial connection.",
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
