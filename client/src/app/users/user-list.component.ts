import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User, UserRole } from './user';
import { UserService } from './user.service';
import { Subject, takeUntil } from 'rxjs';
import { RouterLink } from '@angular/router';
import {
  MatNavList,
  MatListSubheaderCssMatStyler,
  MatListItem,
  MatListItemAvatar,
  MatListItemTitle,
  MatListItemLine,
} from '@angular/material/list';
import { UserCardComponent } from './user-card.component';

import { MatRadioGroup, MatRadioButton } from '@angular/material/radio';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MatInput } from '@angular/material/input';
import { MatFormField, MatLabel, MatHint, MatError } from '@angular/material/form-field';
import { MatCard, MatCardTitle, MatCardContent } from '@angular/material/card';

/**
 * A component that displays a list of users, either as a grid
 * of cards or as a vertical list.
 *
 * The component supports local filtering by name and/or company,
 * and remote filtering (i.e., filtering by the server) by
 * role and/or age. These choices are fairly arbitrary here,
 * but in "real" projects you want to think about where it
 * makes the most sense to do the filtering.
 */
@Component({
  selector: 'app-user-list-component',
  templateUrl: 'user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  providers: [],
  standalone: true,
  imports: [
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatInput,
    FormsModule,
    MatHint,
    MatSelect,
    MatOption,
    MatRadioGroup,
    MatRadioButton,
    UserCardComponent,
    MatNavList,
    MatListSubheaderCssMatStyler,
    MatListItem,
    RouterLink,
    MatListItemAvatar,
    MatListItemTitle,
    MatListItemLine,
    MatError,
  ],
})
export class UserListComponent implements OnInit, OnDestroy {
  // These are public so that tests can reference them (.spec.ts)
  public serverFilteredUsers: User[];
  public filteredUsers: User[];

  public userName: string;
  public userAge: number;
  public userRole: UserRole;
  public userCompany: string;
  public viewType: 'card' | 'list' = 'card';

  errMsg = '';
  private ngUnsubscribe = new Subject<void>();

  /**
   * This constructor injects both an instance of `UserService`
   * and an instance of `MatSnackBar` into this component.
   *
   * @param userService the `UserService` used to get users from the server
   * @param snackBar the `MatSnackBar` used to display feedback
   */
  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    // Nothing here – everything is in the injection parameters.
  }

  /**
   * Get the users from the server, filtered by the role and age specified
   * in the GUI.
   */
  getUsersFromServer() {
    // A user-list-component is paying attention to userService.getUsers()
    // (which is an Observable<User[]>).
    // (For more on Observable, see: https://reactivex.io/documentation/observable.html)
    this.userService
      .getUsers({
        // Filter the users by the role and age specified in the GUI
        role: this.userRole,
        age: this.userAge,
      })
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe({
        // Next time we see a change in the Observable<User[]>,
        // refer to that User[] as returnedUsers here and do the steps in the {}
        next: returnedUsers => {
          // First, update the array of serverFilteredUsers to be the User[] in the observable
          this.serverFilteredUsers = returnedUsers;
          // Then update the filters for our client-side filtering as described in this method
          this.updateFilter();
        },
        // If we observe an error in that Observable, put that message in a snackbar so we can learn more
        error: err => {
          if (err.error instanceof ErrorEvent) {
            this.errMsg = `Problem in the client – Error: ${err.error.message}`;
          } else {
            this.errMsg = `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`;
          }
        },
      });
  }

  /**
   * Called when the filtering information is changed in the GUI so we can
   * get an updated list of `filteredUsers`.
   */
  public updateFilter() {
    this.filteredUsers = this.userService.filterUsers(this.serverFilteredUsers, {
      name: this.userName,
      company: this.userCompany,
    });
  }

  /**
   * Starts an asynchronous operation to update the users list
   */
  ngOnInit(): void {
    this.getUsersFromServer();
  }

  /**
   * When this component is destroyed, we should unsubscribe to any
   * outstanding requests.
   */
  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
