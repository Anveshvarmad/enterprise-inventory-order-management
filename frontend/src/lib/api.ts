import type {
  AnalyticsDashboard,
  AuthResponse,
  Category,
  Customer,
  DashboardSummary,
  InventoryItem,
  Order,
  OrderCreateRequest,
  PageResponse,
  Product,
  ProductCreateRequest,
  StockAdjustmentRequest,
  Supplier,
  Warehouse
} from "../types";
import { getToken } from "./auth";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

function authHeaders() {
  const token = getToken();

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;

    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      // keep default message
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function login(email: string, password: string) {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  });

  return handleResponse<AuthResponse>(response);
}

export async function apiGet<T>(path: string) {
  const response = await fetch(`${API_URL}${path}`, {
    headers: authHeaders()
  });

  return handleResponse<T>(response);
}

export async function apiPost<T>(path: string, body?: unknown) {
  const response = await fetch(`${API_URL}${path}`, {
    method: "POST",
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined
  });

  return handleResponse<T>(response);
}

export async function apiPut<T>(path: string, body?: unknown) {
  const response = await fetch(`${API_URL}${path}`, {
    method: "PUT",
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined
  });

  return handleResponse<T>(response);
}

export async function apiDelete<T>(path: string) {
  const response = await fetch(`${API_URL}${path}`, {
    method: "DELETE",
    headers: authHeaders()
  });

  return handleResponse<T>(response);
}

export async function graphql<T>(query: string, variables?: Record<string, unknown>) {
  const response = await fetch(`${API_URL}/graphql`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ query, variables })
  });

  const result = await handleResponse<{ data?: T; errors?: { message: string }[] }>(response);

  if (result.errors && result.errors.length > 0) {
    throw new Error(result.errors[0].message);
  }

  if (!result.data) {
    throw new Error("No GraphQL data returned");
  }

  return result.data;
}

export function getProducts() {
  return apiGet<PageResponse<Product>>("/api/products?page=0&size=50");
}

export function createProduct(body: ProductCreateRequest) {
  return apiPost<Product>("/api/products", body);
}

export function deleteProduct(id: number) {
  return apiDelete<void>(`/api/products/${id}`);
}

export function getInventory() {
  return apiGet<PageResponse<InventoryItem>>("/api/inventory?page=0&size=50");
}

export function adjustStock(body: StockAdjustmentRequest) {
  return apiPost<InventoryItem>("/api/inventory/adjust", body);
}

export function getOrders() {
  return apiGet<PageResponse<Order>>("/api/orders?page=0&size=50");
}

export function createOrder(body: OrderCreateRequest) {
  return apiPost<Order>("/api/orders", body);
}

export function markOrderPaid(id: number) {
  return apiPost<Order>(`/api/orders/${id}/mark-paid`);
}

export function shipOrder(id: number) {
  return apiPost<Order>(`/api/orders/${id}/ship`);
}

export function cancelOrder(id: number) {
  return apiPost<Order>(`/api/orders/${id}/cancel`);
}

export function getCategories() {
  return apiGet<Category[]>("/api/lookups/categories");
}

export function getSuppliers() {
  return apiGet<Supplier[]>("/api/lookups/suppliers");
}

export function getWarehouses() {
  return apiGet<Warehouse[]>("/api/lookups/warehouses");
}

export function getCustomers() {
  return apiGet<Customer[]>("/api/lookups/customers");
}

export async function getDashboardSummary() {
  const query = `
    query DashboardSummary {
      dashboardSummary {
        totalProducts
        activeProducts
        lowStockItems
        totalOrders
        pendingOrders
        shippedOrders
        totalCustomers
        totalWarehouses
        totalInventoryUnits
        totalOrderValue
      }
    }
  `;

  const data = await graphql<{ dashboardSummary: DashboardSummary }>(query);
  return data.dashboardSummary;
}


export function getAnalyticsDashboard() {
  return apiGet<AnalyticsDashboard>("/api/analytics/dashboard");
}
