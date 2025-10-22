import { Routes } from '@angular/router';
import { AdminDashboard } from './features/admin/admin-dashboard/admin-dashboard';
import { CustomerOrder } from './features/customer/customer-order/customer-order';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'customer',
        pathMatch: 'full'
    },
    {
        path: 'customer',
        component: CustomerOrder,
        title: 'สั่งอาหาร - Food Order System'
    },
    {
        path: 'admin',
        component: AdminDashboard,
        title: 'จัดการออเดอร์ - Food Order System'
    },
    {
        path: '**',
        redirectTo: 'customer'
    }
];
