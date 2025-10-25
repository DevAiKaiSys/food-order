export interface MenuItem {
  id: number;
  menu_name: string;
  price: number;
  image: string;
  category?: string;
}

export interface CartItem extends MenuItem {
  qty: number;
}

export interface Order {
  id: number;
  slip_id: string;
  customer_name: string;
  phone: string;
  item_count: number;
  total_amount: number;
  status: OrderStatus;
  created_at: string;
  details?: CartItem[];
}

export interface OrderDetail {
  id: number;
  slip_id: string;
  customer: {
    id: number;
    name: string;
    phone: string;
  };
  status: OrderStatus;
  total_amount: number;
  items: OrderItem[];
  created_at: string;
  updated_at: string;
}

export interface OrderItem {
  menu_name: string;
  quantity: number;
  price: number;
  total: number;
}

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'COOKING'
  | 'DELIVERING'
  | 'COMPLETED'
  | 'CANCELLED';

export interface StatusConfig {
  label: string;
  color: string;
  icon: string;
  nextStatus?: OrderStatus;
}