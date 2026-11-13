import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VendorApply } from './vendor-apply';

describe('VendorApply', () => {
  let component: VendorApply;
  let fixture: ComponentFixture<VendorApply>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VendorApply]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VendorApply);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
