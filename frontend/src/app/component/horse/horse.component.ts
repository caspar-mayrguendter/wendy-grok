import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent
],
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit, OnDestroy {
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;
  searchForm: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private fb: FormBuilder
  ) {
    this.searchForm = this.fb.group({
      name: [''],
      description: [''],
      bornBefore: [''],
      sex: [''],
      ownerName: ['']
    });
  }

  ngOnInit(): void {
    this.reloadHorses();

    // Set up debounced search
    this.searchForm.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.searchHorses();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  reloadHorses() {
    this.searchHorses();
  }

  searchHorses() {
    const searchParams = this.searchForm.value;
    this.service.search(searchParams)
      .subscribe({
        next: data => {
          this.horses = data;
          this.bannerError = null; // Clear any previous errors
        },
        error: error => {
          console.error('Error searching horses', error);
          this.bannerError = 'Could not search horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Search Horses');
        }
      });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }


  deleteHorse(horse: Horse) {
    if (horse && horse.id) {
      this.service.delete(horse.id).subscribe({
        next: () => {
          this.notification.success(`Horse ${horse.name} successfully deleted.`);
          this.reloadHorses();
        },
        error: error => {
          console.error('Error deleting horse', error);
          this.notification.error('Could not delete horse', 'Error');
        }
      });
    }
  }
}
