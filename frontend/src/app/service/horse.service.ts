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
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
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

}
