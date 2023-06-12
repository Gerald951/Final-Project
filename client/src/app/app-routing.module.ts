import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './components/search.component';
import { DisplayComponent } from './components/display.component';
import { MapComponent } from './components/map.component';

const routes: Routes = [
  {path:'', component:SearchComponent},
  {path:'display/:id/:destination', component:DisplayComponent},
  {path: 'map/:object', component:MapComponent, data:{paramTypes : {object : 'object'}}},
  {path: "**", redirectTo:'/', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
