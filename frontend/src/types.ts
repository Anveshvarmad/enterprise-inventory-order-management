export type UserRole =
  | "ADMIN"
  | "MANAGER"
  | "WAREHOUSE_STAFF"
  | "SALES_USER"
  | "CUSTOMER_SUPPORT";

export type AuthResponse = {
  token: string;
  tokenType: string;
  userId: number;
  fullName: string;
  email: string;
  role: UserRole;
};

export type CurrentUser = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type Category = {
  id: number;
  name: string;
  description?: string;
};

export type Supplier = {
  id: number;
  name: string;
  contactEmail?: string;
  phone?: string;
  status: string;
};

export type Warehouse = {
  id: number;
  name: string;
  code: string;
  location?: string;
  capacity: number;
  status: string;
};

export type Customer = {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone?: string;
  status: string;
};

export type Product = {
  id: number;
  sku: string;
  name: string;
  description?: string;
  unitPrice: number | string;
  reorderLevel: number;
  status: string;
  category?: Category;
  supplier?: Supplier;
};

export type InventoryItem = {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  quantityOnHand: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderLevel: number;
  lowStock: boolean;
};

export type OrderItem = {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  quantity: number;
  unitPrice: number | string;
  lineTotal: number | string;
};

export type Order = {
  id: number;
  orderNumber: string;
  orderStatus: string;
  paymentStatus: string;
  shipmentStatus: string;
  totalAmount: number | string;
  customer: Customer;
  items: OrderItem[];
};

export type DashboardSummary = {
  totalProducts: number;
  activeProducts: number;
  lowStockItems: number;
  totalOrders: number;
  pendingOrders: number;
  shippedOrders: number;
  totalCustomers: number;
  totalWarehouses: number;
  totalInventoryUnits: number;
  totalOrderValue: string;
};

export type ProductCreateRequest = {
  sku: string;
  name: string;
  description?: string;
  categoryId: number;
  supplierId: number;
  unitPrice: number;
  reorderLevel: number;
};

export type StockAdjustmentRequest = {
  productId: number;
  warehouseId: number;
  quantityChange: number;
  notes?: string;
};

export type OrderCreateRequest = {
  customerId: number;
  items: {
    productId: number;
    warehouseId: number;
    quantity: number;
  }[];
  notes?: string;
};

export type RevenueTrendPoint = {
  period: string;
  totalOrders: number;
  totalRevenue: string;
};

export type OrderStatusBreakdown = {
  status: string;
  count: number;
};

export type TopProductAnalytics = {
  productId: number;
  sku: string;
  productName: string;
  totalQuantitySold: number;
  totalRevenue: string;
};

export type WarehouseInventoryAnalytics = {
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  quantityOnHand: number;
  reservedQuantity: number;
  availableQuantity: number;
};

export type LowStockRisk = {
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  availableQuantity: number;
  reorderLevel: number;
};

export type AnalyticsDashboard = {
  revenueTrend: RevenueTrendPoint[];
  orderStatusBreakdown: OrderStatusBreakdown[];
  topProducts: TopProductAnalytics[];
  warehouseInventory: WarehouseInventoryAnalytics[];
  lowStockRisk: LowStockRisk[];
};

export type DemandForecastItem = {
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  quantityOnHand: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderLevel: number;
  predictedDemand7Days: number;
  predictedDailyDemand: number;
  estimatedDaysUntilStockout: number;
  recommendedReorderQuantity: number;
  riskLevel: "HIGH" | "MEDIUM" | "LOW";
};

export type DemandForecast = {
  generatedAt: string;
  modelName: string;
  modelVersion: string;
  totalItemsScored: number;
  highRiskItems: number;
  mediumRiskItems: number;
  lowRiskItems: number;
  forecasts: DemandForecastItem[];
};
