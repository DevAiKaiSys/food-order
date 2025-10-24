import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { CustomerOrder } from './customer-order';
import { provideHttpClient } from '@angular/common/http';

describe('CustomerOrder', () => {
  let component: CustomerOrder;
  let fixture: ComponentFixture<CustomerOrder>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerOrder],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CustomerOrder);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
