import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserCardComponent } from './user-card.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';

import { User } from './user';

describe('UserCardComponent', () => {
  let component: UserCardComponent;
  let fixture: ComponentFixture<UserCardComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule, MatCardModule, UserCardComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserCardComponent);
    component = fixture.componentInstance;
    const user: User = {
      _id: 'chris_id',
      name: 'Chris',
      age: 25,
      company: 'UMM',
      email: 'chris@this.that',
      role: 'admin',
      avatar: 'https://gravatar.com/avatar/8c9616d6cc5de638ea6920fb5d65fc6c?d=identicon',
    };
    // This is a semi-weird hack to get around the (current) inability to write to input
    // signals. Hopefully once that is fixed, we should be able to do something more like
    // the commented out version below. I found this StackOverflow post helpful:
    // https://stackoverflow.com/questions/77838277/how-to-initialise-angular-components-with-signal-inputs-from-test
    fixture.componentRef.setInput('user', user);
    // component.user = input(user);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
