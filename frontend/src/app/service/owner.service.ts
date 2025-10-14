import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Owner, OwnerCreate } from '../dto/owner';

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})
export class OwnerService {

  constructor(
    private http: HttpClient
  ) { }

  /**
   * Search owners by name (for owner selection)
   *
   * @param name the name to search for
   * @param limit maximum number of results (default 5)
   * @return observable list of matching owners
   */
  searchByName(name: string, limit: number = 5): Observable<Owner[]> {
    const params = new URLSearchParams();
    if (name) {
      params.set('name', name);
    }
    params.set('maxAmount', limit.toString());

    return this.http.get<Owner[]>(`${baseUri}?${params.toString()}`);
  }

  /**
   * Create a new owner in the system.
   *
   * @param owner the data for the owner that should be created
   * @return an Observable for the created owner
   */
  create(owner: OwnerCreate): Observable<Owner> {
    return this.http.post<Owner>(baseUri, owner);
  }
}