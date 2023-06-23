import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Carpark } from '../model/carpark';
import { SearchService } from '../services/search.service';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  carP! : Carpark
  notDeleted : Boolean = false
  id! : string
  constructor(private activatedRoute : ActivatedRoute, private router : Router, private searchSvc : SearchService) {

  }

  ngOnInit(): void {
      this.activatedRoute.params.subscribe(params => {
        const cP = JSON.parse(params['carPark'])

        this.carP = {
          carParkId : cP.carParkId,
          address : cP.address,
          latitude : cP.latitude,
          longitude : cP.longitude,
          distance : cP.distance,
          cost : cP.cost,
          lotsAvailable : cP.lotsAvailable
        } as Carpark

        this.id = params['id'] as string

      })
  }

  return() {
    this.searchSvc.deleteRecord(this.id).then((value : any) => {
                            // const jo = JSON.parse(value)

                            if ('OK' in value) {
                              this.router.navigate(['/search'])
                            } else {
                              this.notDeleted = true
                            }
    })
  }
}
