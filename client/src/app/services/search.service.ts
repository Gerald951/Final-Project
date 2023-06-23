import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, lastValueFrom, map } from 'rxjs'
import { Destination } from '../model/destination';
import { Carpark } from '../model/carpark';

const SERVER_URL = "http://localhost:8080/api"

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' })
};

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  destination! : Destination
  carparkList : Carpark[] = []
  getLot$! : Promise<string>

  constructor(private http: HttpClient) { }

  getLotAvailability(destinationId : string, carparkId : string) {
    const params = new HttpParams().set("destinationId", destinationId).set("carparkId", carparkId)
    this.getLot$ = lastValueFrom(this.http.get<string>(SERVER_URL+'/search/lot', {params}))
    return this.getLot$
  }

  searchNearbyCarParks(d : Destination) : Observable<HttpResponse<string>> {
    const body = new HttpParams().set('id', d.id)
                                  .set('destination', d.destination)
                                  .set('distance', d.distance.toString())
                                  .set('listOfParkedTime', d.listOfParkedTime.join(", "))
                                  .set('listOfExitTime', d.listOfExitTime.join(', '))
                                  .set('dayOfWeek', d.dayOfWeek.join(', '))

    const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')

    return this.http.post<string>(SERVER_URL + '/search/redirect', body.toString(), {headers:headers, observe:'response'})
  }

  getNearbyCP(id : string, distance : string, filter ='', sortOrder='asc', pageNumber = 0, pageSize = 3) : Observable<any> {
    return this.http.get<Carpark[]>(SERVER_URL + '/search/' + id + '/' + distance)
   
    
    }

    deleteRecord(id : string) : Promise<string> {
      return lastValueFrom(this.http.delete<string>(SERVER_URL +'/delete/' + id))
    }
  }

