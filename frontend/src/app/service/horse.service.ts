import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Horse, HorseCreate, HorseUpdate } from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient
  ) { }

  /**
   * Search horses based on criteria
   *
   * @param searchParams optional search parameters
   * @return observable list of found horses.
   */
  search(searchParams?: any): Observable<Horse[]> {
    let params = new URLSearchParams();

    if (searchParams) {
      if (searchParams.name) params.set('name', searchParams.name);
      if (searchParams.description) params.set('description', searchParams.description);
      if (searchParams.bornBefore) params.set('bornBefore', searchParams.bornBefore);
      if (searchParams.sex) params.set('sex', searchParams.sex);
      if (searchParams.ownerName) params.set('ownerName', searchParams.ownerName);
    }

    const queryString = params.toString();
    const url = queryString ? `${baseUri}?${queryString}` : baseUri;

    return this.http.get<Horse[]>(url);
  }

  /**
   * Get all horses stored in the system (legacy method)
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.search();
  }

  /**
   * Search horses by name (for parent selection)
   *
   * @param name the name to search for
   * @param limit maximum number of results (default 5)
   * @return observable list of matching horses
   */
  searchByName(name: string, limit: number = 5): Observable<Horse[]> {
    const params = new URLSearchParams();
    if (name) {
      params.set('name', name);
    }
    params.set('limit', limit.toString());

    return this.http.get<Horse[]>(`${baseUri}?${params.toString()}`);
  }


  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: HorseCreate
  ): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  /**
   * Get a horse by its ID.
   *
   * @param id the ID of the horse to retrieve
   * @return an Observable for the horse
   */
  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Update an existing horse in the system.
   *
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: HorseUpdate): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${horse.id}`,
      horse
    );
  }

  /**
   * Delete a horse from the system.
   *
   * @param id the ID of the horse to delete
   * @return an Observable for the delete operation
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }

}
