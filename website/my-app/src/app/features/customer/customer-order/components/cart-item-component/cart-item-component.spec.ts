import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CartItemComponent } from './cart-item-component';

describe('CartItemComponent', () => {
  let component: CartItemComponent;
  let fixture: ComponentFixture<CartItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartItemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CartItemComponent);
    component = fixture.componentInstance;
    component.item = {
        id: 1,
        menuName: 'ข้าวมันไก่',
        quantity: 1,
        price: 50
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
