import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { OwnerService } from 'src/app/service/owner.service';
import { Owner } from 'src/app/dto/owner';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss'],
  imports: [
    FormsModule,
    RouterLink
  ]
})
export class OwnerCreateComponent {
  owner: Owner = {
    firstName: '',
    lastName: '',
    email: ''
  };

  constructor(
    private service: OwnerService,
    private router: Router,
    private notification: ToastrService
  ) {}

  public onSubmit(form: NgForm): void {
    if (form.valid) {
      // Clear empty email
      if (this.owner.email === '') {
        delete this.owner.email;
      }

      this.service.create(this.owner as any).subscribe({
        next: data => {
          this.notification.success(`Owner ${data.firstName} ${data.lastName} successfully created.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error creating owner', error);

          // Handle validation errors (422 status)
          if (error.status === 422 && error.error && error.error.errors) {
            const validationErrors = error.error.errors;
            this.notification.error(`Validation failed: ${validationErrors.join(', ')}`, 'Validation Error');
          } else {
            this.notification.error('Could not create owner', 'Error');
          }
        }
      });
    }
  }

  public dynamicCssClassesForInput(input: any): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }
}
