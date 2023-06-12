import { Component, OnInit } from '@angular/core';
import { Carpark } from '../model/carpark';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit{
  carPark! : Carpark

  constructor(private activatedRoute : ActivatedRoute) {}

  ngOnInit(): void {
      this.activatedRoute.params.subscribe(params => {
        this.carPark = params['object']
      })
  }
}
