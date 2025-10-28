import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Horse, HorseCreate, HorseUpdate, HorseFamilyTree } from '../dto/horse';
import { Sex } from '../dto/sex';

const baseUri = environment.backendUrl + '/horses';

// Backend request interfaces
interface HorseCreateBackend {
  name: string;
  description?: string;
  dateOfBirth: string;
  sex: string;
  ownerId?: number;
  parentIds?: number[];
}

interface HorseUpdateBackend {
  id: number;
  name: string;
  description?: string;
  dateOfBirth: string;
  sex: string;
  ownerId?: number;
  parentIds?: number[];
}

// Backend response interfaces
interface HorseListBackend {
  id: number;
  name: string;
  description?: string;
  dateOfBirth: string;
  sex: string;
  owner?: {
    id: number;
    firstName: string;
    lastName: string;
    email?: string;
  };
}

interface ParentBackend {
  horse: HorseListBackend;
  relationship: string;
}

interface HorseDetailBackend {
  id: number;
  name: string;
  description?: string;
  dateOfBirth: string;
  sex: string;
  owner?: {
    id: number;
    firstName: string;
    lastName: string;
    email?: string;
  };
  parents: ParentBackend[];
}

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

    return this.http.get<HorseListBackend[]>(url).pipe(
      map(response => response.map(item => this.convertHorseListBackendToHorse(item)))
    );
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

    return this.http.get<HorseListBackend[]>(`${baseUri}?${params.toString()}`).pipe(
      map(response => response.map(item => this.convertHorseListBackendToHorse(item)))
    );
  }


  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: HorseCreate): Observable<Horse> {
    const backendRequest: HorseCreateBackend = {
      name: horse.name,
      description: horse.description,
      dateOfBirth: horse.dateOfBirth.toISOString().split('T')[0],
      sex: horse.sex,
      ownerId: horse.ownerId,
      parentIds: horse.parentIds
    };

    return this.http.post<HorseDetailBackend>(
      baseUri,
      backendRequest
    ).pipe(
      map(response => this.convertHorseDetailBackendToHorse(response))
    );
  }

  /**
   * Get a horse by its ID.
   *
   * @param id the ID of the horse to retrieve
   * @return an Observable for the horse
   */
  getById(id: number): Observable<Horse> {
    return this.http.get<HorseDetailBackend>(`${baseUri}/${id}`).pipe(
      map(response => this.convertHorseDetailBackendToHorse(response))
    );
  }

  /**
   * Get the family tree for a horse.
   *
   * @param id the ID of the horse
   * @param maxGenerations maximum number of generations to include (default 5)
   * @return an Observable for the family tree
   */
  getFamilyTree(id: number, maxGenerations: number = 5): Observable<HorseFamilyTree> {
    return this.http.get<HorseFamilyTree>(`${baseUri}/${id}/family-tree?maxGenerations=${maxGenerations}`);
  }

  /**
   * Update an existing horse in the system.
   *
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: HorseUpdate): Observable<Horse> {
    const backendRequest: HorseUpdateBackend = {
      id: horse.id,
      name: horse.name,
      description: horse.description,
      dateOfBirth: horse.dateOfBirth.toISOString().split('T')[0],
      sex: horse.sex,
      ownerId: horse.ownerId,
      parentIds: horse.parentIds
    };

    return this.http.put<HorseDetailBackend>(
      `${baseUri}/${horse.id}`,
      backendRequest
    ).pipe(
      map(response => this.convertHorseDetailBackendToHorse(response))
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

  /**
   * Convert backend HorseListBackend to frontend Horse format
   */
  private convertHorseListBackendToHorse(backend: HorseListBackend): Horse {
    return {
      id: backend.id,
      name: backend.name,
      description: backend.description,
      dateOfBirth: new Date(backend.dateOfBirth),
      sex: backend.sex === 'FEMALE' ? Sex.female : Sex.male,
      owner: backend.owner ? {
        id: backend.owner.id,
        firstName: backend.owner.firstName,
        lastName: backend.owner.lastName,
        email: backend.owner.email
      } : undefined,
      mother: undefined,
      father: undefined
    };
  }

  /**
   * Convert backend HorseDetailBackend to frontend Horse format
   */
  private convertHorseDetailBackendToHorse(backend: HorseDetailBackend): Horse {
    // Find mother and father from parents array
    let mother: Horse | undefined;
    let father: Horse | undefined;

    if (backend.parents) {
      for (const parent of backend.parents) {
        const parentHorse: Horse = {
          id: parent.horse.id,
          name: parent.horse.name,
          description: parent.horse.description,
          dateOfBirth: new Date(parent.horse.dateOfBirth),
          sex: parent.horse.sex === 'FEMALE' ? Sex.female : Sex.male,
          owner: parent.horse.owner ? {
            id: parent.horse.owner.id,
            firstName: parent.horse.owner.firstName,
            lastName: parent.horse.owner.lastName,
            email: parent.horse.owner.email
          } : undefined,
          mother: undefined,
          father: undefined
        };

        if (parent.relationship === 'mother') {
          mother = parentHorse;
        } else if (parent.relationship === 'father') {
          father = parentHorse;
        }
      }
    }

    return {
      id: backend.id,
      name: backend.name,
      description: backend.description,
      dateOfBirth: new Date(backend.dateOfBirth),
      sex: backend.sex === 'FEMALE' ? Sex.female : Sex.male,
      owner: backend.owner ? {
        id: backend.owner.id,
        firstName: backend.owner.firstName,
        lastName: backend.owner.lastName,
        email: backend.owner.email
      } : undefined,
      mother: mother,
      father: father
    };
  }

}
