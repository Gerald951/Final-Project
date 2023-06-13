import { BehaviorSubject, Observable, catchError, finalize, of, Subscription } from "rxjs";
import { SearchService } from "../services/search.service";
import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { Carpark } from "./carpark";
import { Destination } from "./destination";
import { HttpResponse } from "@angular/common/http";

export class ResultsDataSource implements DataSource<Carpark> {

    private ResultsSubject = new BehaviorSubject<Carpark[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);

    public loading$ = this.loadingSubject.asObservable();
    sub$! : Subscription

    dest : Destination = {
        id : "",
        destination : "",
        distance : 200,
        listOfParkedTime : [],
        listOfExitTime : [],
        dayOfWeek : [],
      }
    
    resultList : Carpark[] = []

    constructor(private searchSvc : SearchService) {}

    connect(collectionViewer: CollectionViewer): Observable<Carpark[]> {
        return this.ResultsSubject.asObservable();
    }

    disconnect(collectionViewer: CollectionViewer): void {
        this.ResultsSubject.complete();
        this.loadingSubject.complete();
    }

    loadResults(courseId: string, filter = '',
                sortDirection = 'asc', pageIndex = 0, pageSize = 3) {

        this.loadingSubject.next(true);

        this.searchSvc.getNearbyCP(courseId, '200', filter, sortDirection,
            pageIndex, pageSize).pipe(
            catchError(() => of([])),
            finalize(() => this.loadingSubject.next(false))
        )
        .subscribe(results => {
            let listOfCarParks = JSON.parse(results.listOfCarParks)
            // Create an array to hold the result objects

            // Iterate over the listOfCarParks and push each object into the resultList
            listOfCarParks.forEach((carpark: any) => {
            this.resultList.push({
                carParkId: carpark.carParkId,
                address: carpark.address,
                latitude: carpark.latitude,
                longitude: carpark.longitude,
                distance: carpark.distance,
                cost: carpark.cost,
                lotsAvailable: carpark.lotsAvailable
            });
            });

            this.ResultsSubject.next(this.resultList)
        
            let listOfParkedTime = JSON.parse(results.listOfParkedTime)
            let listOfExitTime = JSON.parse(results.listOfExitTime)
            let dayOfWeek = JSON.parse(results.dayOfWeek)

            listOfParkedTime.map((p : string) => {this.dest.listOfParkedTime.push(p)})
            listOfExitTime.map((e : string) => {this.dest.listOfExitTime.push(e)})
            dayOfWeek.map((d : string) => {this.dest.dayOfWeek.push(d)})
            this.dest.id = results.id
            this.dest.destination = results.destination
            this.dest.distance = results.distance
        });


    }    

    sortResults(sortBy: string) {
        console.info("sort2");
        this.resultList.sort((a, b) => {
          if (sortBy === 'distance') {
            const distanceA = a.distance;
            const distanceB = b.distance;
            return distanceA - distanceB;
          } else if (sortBy === 'cost') {
            const costA = parseFloat(a.cost);
            const costB = parseFloat(b.cost);
            return costA - costB;
          }
          return 0; // Default to no sorting if sortBy parameter is not provided or invalid
        });
        console.info("sort3");
      
        this.ResultsSubject.next(this.resultList);
      }

    updateResults(id : string, value : number) {
      this.dest.distance = value

      this.sub$ = this.searchSvc.searchNearbyCarParks(this.dest).subscribe(
        (response: HttpResponse<string>) => {
          if (response.status === 200) {
            // Successful response with status code 200
            console.log('Request succeeded with status code 200');
            this.loadingSubject.next(true);

            this.searchSvc.getNearbyCP(id, value.toString(), '',
            'asc', 0, 3).pipe(
                catchError(() => of([])),
                finalize(() => this.loadingSubject.next(false))
            )
            .subscribe(results => {
                let listOfCarParks = JSON.parse(results.listOfCarParks)
                // Create an array to hold the result objects
                this.resultList = []
                // Iterate over the listOfCarParks and push each object into the resultList
                listOfCarParks.forEach((carpark: any) => {
                this.resultList.push({
                    carParkId: carpark.carParkId,
                    address: carpark.address,
                    latitude: carpark.latitude,
                    longitude: carpark.longitude,
                    distance: carpark.distance,
                    cost: carpark.cost,
                    lotsAvailable: carpark.lotsAvailable
                });
                });

                this.ResultsSubject.next(this.resultList)
            
                let listOfParkedTime = JSON.parse(results.listOfParkedTime)
                let listOfExitTime = JSON.parse(results.listOfExitTime)
                let dayOfWeek = JSON.parse(results.dayOfWeek)

                listOfParkedTime.map((p : string) => {this.dest.listOfParkedTime.push(p)})
                listOfExitTime.map((e : string) => {this.dest.listOfExitTime.push(e)})
                dayOfWeek.map((d : string) => {this.dest.dayOfWeek.push(d)})
                this.dest.id = results.id
                this.dest.destination = results.destination
                this.dest.distance = results.distance
          })
            
          } else {
            // Handle other status codes
            console.log('Request returned status code:', response.status);
          }
        },
        (error) => {
          // Handle error
          console.error('An error occurred:', error);
        }
      )

  }
}