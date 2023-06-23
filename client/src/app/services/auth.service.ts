import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const AUTH_API = 'http://localhost:8080/api/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' })
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    const body = new HttpParams().set('username', username).set('password', password)
    return this.http.post(
      AUTH_API + 'signin',
      body.toString(),
      httpOptions
    );
  }

  register(username: string, email: string, password: string): Observable<any> {
    const body = new HttpParams().set('username', username).set('email', email).set('password', password)
    return this.http.post(
      AUTH_API + 'signup',
      body.toString(),
      httpOptions
    );
  }

  logout(): Observable<any> {
    return this.http.post(AUTH_API + 'signout', {}, httpOptions);
  }
}
