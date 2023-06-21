import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './components/search.component';
import { DisplayComponent } from './components/display.component';
import { MapComponent } from './components/map.component';
import { SummaryComponent } from './components/summary.component';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';

const routes: Routes = [
  {path:'', component:LoginComponent},
  {path:'register', component:RegisterComponent},
  {path:'search', component:SearchComponent},
  {path:'display/:id/:destination', component:DisplayComponent},
  {path: 'map/:object/:id', component:MapComponent},
  {path: 'summary/:carPark/:id', component:SummaryComponent},
  {path: "**", redirectTo:'/', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
