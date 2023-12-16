import { TestBed } from '@angular/core/testing';

import { Auth2factorService } from './auth2factor.service';

describe('Auth2factorService', () => {
  let service: Auth2factorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Auth2factorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
