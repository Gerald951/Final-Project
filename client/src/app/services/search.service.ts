import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, lastValueFrom, map } from 'rxjs'
import { Destination } from '../model/destination';
import { Carpark } from '../model/carpark';

const SERVER_URL = "http://localhost:8080"

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  destination! : Destination
  carparkList : Carpark[] = []
  getLot$! : Promise<string>

  constructor(private http: HttpClient) { }

  getLotAvailability(destination : string, type : string) {
    const params = new HttpParams().set("destination", destination).set("type", type)
    this.getLot$ = lastValueFrom(this.http.get<string>(SERVER_URL+'/search/lot', {params}))
    return this.getLot$
  }

  searchNearbyCarParks(d : Destination) : Observable<HttpResponse<string>> {
    const body = JSON.stringify(d)

    const headers = new HttpHeaders().set('Content-Type', 'application/json; charset=UTF-8')

    return this.http.post<string>(SERVER_URL + '/search/redirect', body, {headers:headers, observe:'response'})
  }

  getNearbyCP(id : string) : Observable<any> {
    return this.http.get<Carpark[]>(SERVER_URL + '/search/' + id)
   
    
    }
  }

