import { test, expect } from '@playwright/test';

test.describe('Food Ordering', () => {
  test.beforeEach(async ({ page }) => {
    // 1. Navigate to the customer page
    await page.goto('/customer');
  });

  test('should allow a user to order food with valid data', async ({
    page,
  }) => {
    // 2. Add the first menu item to the cart
    await page.locator('.card .btn-primary').first().click();

    // 3. Assert that the cart badge shows 1 item
    await expect(page.locator('.badge.bg-light.text-primary').first()).toHaveText('1');

    // 4. Assert cart item details - เลือกเฉพาะใน desktop cart (col-lg-4)
    const desktopCart = page.locator('.col-12.col-lg-4');
    await expect(desktopCart.locator('app-cart-item-component')).toBeVisible();

    // ใช้ selector ที่เจาะจงมากขึ้น - เลือกเฉพาะ span.fw-bold แรก (ชื่อเมนู) ไม่ใช่ span.fw-bold.text-primary (ราคา)
    await expect(
      desktopCart.locator('app-cart-item-component').first().locator('span.fw-bold').first()
    ).toHaveText('ข้าวผัดกุ้ง');

    // 5. Fill in customer details - ใช้ desktop form
    await desktopCart.getByPlaceholder('กรอกชื่อ-นามสกุล').fill('John Doe');
    await desktopCart.getByPlaceholder('เช่น 0812345678').fill('0812345678');

    // 6. Submit the order
    await desktopCart.getByRole('button', { name: 'ยืนยันสั่งซื้อ' }).click();

    // 7. Assert that a success toast is shown
    // รอให้ toast แสดงขึ้นมาก่อน (เพิ่ม timeout เผื่อ API call)
    await expect(page.locator('.toast.show')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('.toast-body')).toContainText('สำเร็จ');
  });

  test('should show validation errors for empty customer details', async ({
    page,
  }) => {
    // 2. Add the first menu item to the cart
    await page.locator('.card .btn-primary').first().click();

    // 3. Submit the order without filling in details
    const desktopCart = page.locator('.col-12.col-lg-4');
    await desktopCart.getByRole('button', { name: 'ยืนยันสั่งซื้อ' }).click();

    // 4. Assert that validation messages are shown (ต้องตรงกับข้อความใน customer-form.html)
    await expect(desktopCart.getByText('กรุณากรอกชื่อลูกค้า')).toBeVisible();
    await expect(desktopCart.getByText('กรุณากรอกเบอร์โทรศัพท์')).toBeVisible();
  });

  test('should allow user to add and remove items from cart', async ({
    page,
  }) => {
    const desktopCart = page.locator('.col-12.col-lg-4');

    // 2. Add the first menu item to the cart
    await page.locator('.card .btn-primary').first().click();
    await expect(page.locator('.badge.bg-light.text-primary').first()).toHaveText('1');

    // 3. Add the second menu item to the cart
    await page.locator('.card .btn-primary').nth(1).click();
    await expect(page.locator('.badge.bg-light.text-primary').first()).toHaveText('2');

    // 4. Remove the first item - ใช้ selector ที่ถูกต้อง
    await desktopCart.locator('app-cart-item-component').first().locator('.btn-link.text-danger').click();
    await expect(page.locator('.badge.bg-light.text-primary').first()).toHaveText('1');

    // 5. Assert the correct item is left in the cart (รายการที่ 2 คือ ส้มตำ)
    await expect(
      desktopCart.locator('app-cart-item-component').first().locator('span.fw-bold').first()
    ).toHaveText('ส้มตำ');

    // 6. Remove the second item
    await desktopCart.locator('app-cart-item-component').first().locator('.btn-link.text-danger').click();

    // 7. Assert the cart is empty
    // Badge จะแสดงค่า 0 แทนที่จะหายไป
    await expect(page.locator('.badge.bg-light.text-primary').first()).toHaveText('0');
    await expect(desktopCart.locator('app-cart-item-component')).not.toBeVisible();
  });
});