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


      })
  }

  return() {
    this.searchSvc.deleteRecord(this.carP.carParkId).then((value) => {
                            const jo = JSON.parse(value)

                            if ('OK' in jo) {
                              this.router.navigate(['/'])
                            } else {
                              this.notDeleted = true
                            }
    })
  }
}
