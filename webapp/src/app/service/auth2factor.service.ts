import { Injectable } from '@angular/core';

import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../models/user.model";
import {environment} from "../environnement/environment";

@Injectable({
  providedIn: 'root'
})
export class Auth2factorService {
 apiUrl = environment.api;
  constructor(private http:HttpClient) {

  }

  registration(data:User):Observable<any>{
    return this.http.post(this.apiUrl+'auth/register',data);
  }
}
