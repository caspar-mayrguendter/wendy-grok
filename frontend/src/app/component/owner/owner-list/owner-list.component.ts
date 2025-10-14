import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { OwnerService } from 'src/app/service/owner.service';
import { Owner } from 'src/app/dto/owner';

@Component({
  selector: 'app-owner-list',
  templateUrl: './owner-list.component.html',
  styleUrls: ['./owner-list.component.scss'],
  imports: [
    RouterLink
  ]
})
export class OwnerListComponent implements OnInit {
  owners: Owner[] = [];
  bannerError: string | null = null;

  constructor(
    private service: OwnerService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadOwners();
  }

  loadOwners(): void {
    // Get all owners by calling search without parameters
    this.service.searchByName('', 1000).subscribe({
      next: data => {
        this.owners = data;
        this.bannerError = null;
      },
      error: error => {
        console.error('Error fetching owners', error);
        this.bannerError = 'Could not fetch owners: ' + error.message;
        const errorMessage = error.status === 0
          ? 'Is the backend up?'
          : error.message.message;
        this.notification.error(errorMessage, 'Could Not Fetch Owners');
      }
    });
  }
}
