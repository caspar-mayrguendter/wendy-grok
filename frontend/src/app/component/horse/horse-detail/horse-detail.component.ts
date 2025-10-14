import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss'],
  imports: [
    RouterLink,
    ConfirmDeleteDialogComponent
  ]
})
export class HorseDetailComponent implements OnInit {
  horse: Horse | null = null;
  horseForDeletion: Horse | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: HorseService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadHorse(+id);
      }
    });
  }

  loadHorse(id: number): void {
    this.service.getById(id).subscribe({
      next: horse => {
        this.horse = horse;
      },
      error: error => {
        console.error('Error loading horse details', error);
        this.notification.error('Could not load horse details', 'Error');
        this.router.navigate(['/horses']);
      }
    });
  }

  deleteHorse(horse: Horse): void {
    if (horse && horse.id) {
      this.service.delete(horse.id).subscribe({
        next: () => {
          this.notification.success(`Horse ${horse.name} successfully deleted.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error deleting horse', error);
          this.notification.error('Could not delete horse', 'Error');
        }
      });
    }
  }

  ownerName(): string {
    return this.horse?.owner
      ? `${this.horse.owner.firstName} ${this.horse.owner.lastName}`
      : 'No owner';
  }

  dateOfBirthAsLocaleDate(): string {
    return this.horse ? new Date(this.horse.dateOfBirth).toLocaleDateString() : '';
  }

  sexAsString(): string {
    return this.horse?.sex === 'FEMALE' ? 'Female' : 'Male';
  }
}
