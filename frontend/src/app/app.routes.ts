import {Routes} from '@angular/router';
import {HorseCreateEditComponent, HorseCreateEditMode} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from './component/horse/horse-detail/horse-detail.component';
import {OwnerCreateComponent} from './component/owner/owner-create/owner-create.component';
import {OwnerListComponent} from './component/owner/owner-list/owner-list.component';

export const routes: Routes = [
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
    {path: 'edit/:id', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.edit}},
    {path: 'detail/:id', component: HorseDetailComponent},
  ]},
  {path: 'owners', children: [
    {path: '', component: OwnerListComponent},
    {path: 'create', component: OwnerCreateComponent},
  ]},
  {path: '**', redirectTo: 'horses'},
];
