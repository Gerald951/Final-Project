import { Component, OnInit, OnDestroy } from '@angular/core';
import { SearchService } from '../services/search.service';
import { ActivatedRoute } from '@angular/router';
import { map, Subscription } from 'rxjs';
import { Destination } from '../model/destination';
import { Carpark } from '../model/carpark';

@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['./display.component.css']
})
export class DisplayComponent implements OnInit, OnDestroy {
  destination_! : Destination
  carparkList : Carpark[] = []
  destination! : string
  id! : string
  sub! : Subscription
  constructor(private searchSvc : SearchService, private activatedRoute : ActivatedRoute) {}

  ngOnInit(): void {
    this.destination = this.activatedRoute.snapshot.params['destination']
    this.id = this.activatedRoute.snapshot.params['id']

    this.sub =  this.searchSvc.getNearbyCP(this.id).pipe(
      map(data => {
        // Map destination object
        this.destination_ = {
          id: data.id,
          destination: data.destination,
          distance: data.distance,
          listOfParkedTime: data.listOfParkedTime.map((parkedTime: string)=> ({ parkedTime })),
          listOfExitTime: data.listOfExitTime.map((exitTime: string) => ({ exitTime })),
          dayOfWeek: data.dayOfWeek.map((day: string) => ({ day }))
        };

         // Map carpark list
         this.carparkList = data.listOfCarParks.map((carpark: { carParkId: any; address: any; latitude: any; longitude: any; distance: any; cost: any; lotsAvailable: any; }) => ({
          carParkId: carpark.carParkId,
          address: carpark.address,
          latitude: carpark.latitude,
          longitude: carpark.longitude,
          distance: carpark.distance,
          cost: carpark.cost,
          lotsAvailable: carpark.lotsAvailable
        }));
      })
    ).subscribe();

    console.info(this.destination)
    console.info(this.carparkList)

  }

  ngOnDestroy(): void {
      this.sub.unsubscribe()
  }
}
