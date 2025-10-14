export interface Owner {
  id?: number;
  firstName: string;
  lastName: string;
  email?: string;
}

export interface OwnerCreate {
  firstName: string;
  lastName: string;
  email?: string;
}
