import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
  mother?: Horse;
  father?: Horse;
}

export interface HorseSearch {
  name?: string;
  // TODO fill in missing fields
}

export interface HorseCreate {
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  ownerId?: number;
  motherId?: number;
  fatherId?: number;
}

export interface HorseUpdate {
  id: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  ownerId?: number;
  motherId?: number;
  fatherId?: number;
}

export function convertFromHorseToCreate(horse: Horse): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    ownerId: horse.owner?.id,
    motherId: horse.mother?.id,
    fatherId: horse.father?.id,
  };
}

export function convertFromHorseToUpdate(horse: Horse): HorseUpdate {
  if (!horse.id) {
    throw new Error('Horse must have an ID for update');
  }
  return {
    id: horse.id,
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    ownerId: horse.owner?.id,
    motherId: horse.mother?.id,
    fatherId: horse.father?.id,
  };
}

export interface HorseFamilyTree {
  id: number;
  name: string;
  dateOfBirth: string;
  sex: string;
  mother?: HorseFamilyTree;
  father?: HorseFamilyTree;
}

