export interface MenuItem {
  id: number;
  menuName: string;
  price: number;
  image: string;
  category?: string;
}

export interface CartItem extends MenuItem {
  qty: number;
}

export interface Order {
  id: string;
  customer_name: string;
  items: number;
  total_amount: number;
  status: OrderStatus;
  created_at: string;
  details?: CartItem[];
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