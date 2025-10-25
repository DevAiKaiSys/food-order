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
  updated_at: string;
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

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  COOKING = 'COOKING',
  DELIVERING = 'DELIVERING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface StatusConfig {
  label: string;
  color: string;
  icon: string;
  nextStatus?: OrderStatus;
}

export const STATUS_CONFIG: { [key in OrderStatus]: StatusConfig } = {
  [OrderStatus.PENDING]: {
    label: 'รอดำเนินการ',
    color: 'warning',
    icon: 'clock',
    nextStatus: OrderStatus.CONFIRMED
  },
  [OrderStatus.CONFIRMED]: {
    label: 'ยืนยันออเดอร์',
    color: 'info',
    icon: 'check-circle',
    nextStatus: OrderStatus.COOKING
  },
  [OrderStatus.COOKING]: {
    label: 'เริ่มทําอาหาร',
    color: 'primary',
    icon: 'fire',
    nextStatus: OrderStatus.DELIVERING
  },
  [OrderStatus.DELIVERING]: {
    label: 'กําลังจัดส่ง',
    color: 'secondary',
    icon: 'truck-front',
    nextStatus: OrderStatus.COMPLETED
  },
  [OrderStatus.COMPLETED]: {
    label: 'ออเดอร์เสร็จสิ้น',
    color: 'success',
    icon: 'check-circle-fill'
  },
  [OrderStatus.CANCELLED]: {
    label: 'ยกเลิกออเดอร์',
    color: 'danger',
    icon: 'x-circle'
  }
};