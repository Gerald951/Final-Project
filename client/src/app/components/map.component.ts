/// <reference types="@types/google.maps" />

import { Component, OnInit } from '@angular/core';
import { Carpark } from '../model/carpark';
import { ActivatedRoute } from '@angular/router';
import { MapDirectionsService } from '@angular/google-maps';
import { Observable, map } from 'rxjs';


@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit{
  cp! : Carpark
  map!: google.maps.Map;
  directionsResults$! : Observable<google.maps.DirectionsResult|undefined>
  center: google.maps.LatLngLiteral = {lat: 0, lng: 0};
  zoom = 4;
  destinationOptions: string[] = ["Harbourfront Centre", "Resorts World Sentosa", "VivoCity P2", "VivoCity P3", "Sentosa", "Westgate", "IMM Building", "JCube", "National Gallery", "Singapore Flyer", 
  "Millenia Singapore", "The Esplanade", "Raffles City", "Marina Square", "Suntec City", "Marina Bay Sands", "Centrepoint", "Cineleisure", "Orchard Point", "Concorde Hotel", "Plaza Singapura",
  "The Cathay", "Mandarin Hotel", "Wisma Atria", "The Heeren", "Ngee Ann City", "Orchard Central", "Wheelock Place", "Orchard Gateway", "Tang Plaza", "Far East Plaza", "Paragon",
  "313@Somerset", "The Atrium@Orchard", "Bukit Panjang Plaza", "Clarke Quay", "The Star Vista", "Funan Mall", "Lot One", "Tampines Mall", "Junction 8", "Bedok Mall", "Bugis+"];


  constructor(private activatedRoute : ActivatedRoute, private mapDirectionService : MapDirectionsService) {}

  ngOnInit(): void {
      this.activatedRoute.params.subscribe(params => {
        console.info(params['object'])
        const cP = JSON.parse(params['object']) as Carpark

        const carPark = {
          carParkId : cP.carParkId,
          address : cP.address,
          latitude : cP.latitude,
          longitude : cP.longitude,
          distance : cP.distance,
          cost : cP.cost,
          lotsAvailable : cP.lotsAvailable
        } as Carpark

        console.info(carPark)
        this.cp = carPark
        this.initializeMap()
      })

      
  }

  initializeMap() {
    // Create a new map instance
    console.info('create a new map')
    this.map = new google.maps.Map(document.getElementById('map')!, {
      center: { lat: 0, lng: 0 },
      zoom: 15
    });

    // Get the user's current location

    if (navigator.geolocation) {
      console.info('geolocation received')
      navigator.geolocation.getCurrentPosition((position) => {
        const { latitude, longitude } = position.coords;
        const pos = { lat: latitude, lng: longitude };
        console.info(pos)

        const directionsService = new google.maps.DirectionsService();
        const directionsRenderer = new google.maps.DirectionsRenderer({
        map: this.map,
        panel: document.getElementById('renderer') as HTMLElement
       });

        // create map with original 
        const request: google.maps.DirectionsRequest = {
          destination: {lat: parseFloat(this.cp.latitude), lng: parseFloat(this.cp.longitude)},
          origin : {lat: pos.lat, lng: pos.lng},
          travelMode: google.maps.TravelMode.DRIVING
        }

        console.info(request)

        if (typeof google !== 'undefined' && typeof google.maps !== 'undefined') {
          // Google Maps API is loaded
          // Place your code that depends on the API here
          directionsService.route(request, (response, status) => {
            if (status === google.maps.DirectionsStatus.OK) {
              directionsRenderer.setDirections(response);
              this.trackLocation(directionsRenderer);
            } else {
              console.error('Error fetching directions:', status);
            }
          })

        } else {
          // Google Maps API is not loaded
          // Handle the situation accordingly
          console.info('api is not loaded')
        }
        
        


        // Add a marker to the map
        // new google.maps.Marker({
        //   position: pos,
        //   map: this.map,i don
        //   title: 'Current Location'
        // });
      }, () => {
        // Handle geolocation error
        console.error('Error: The Geolocation service failed.');
      });
    } else {
      // Browser doesn't support geolocation
      console.error('Error: Your browser doesn\'t support geolocation.');
    }
  }

  trackLocation(directionsRenderer: google.maps.DirectionsRenderer) {
    if (navigator.geolocation) {
      const watchId = navigator.geolocation.watchPosition((position) => {
        const { latitude, longitude } = position.coords;
        const pos = { lat: latitude, lng: longitude };
        this.map.setCenter(pos);
        this.map.setZoom(15);
  
        directionsRenderer.setOptions({
          suppressMarkers: true,
          preserveViewport: true,
          polylineOptions: {
            strokeColor: 'blue',
            strokeOpacity: 0.7,
            strokeWeight: 3
          }
        });
  
        const updatedRequest: google.maps.DirectionsRequest = {
          destination: { lat: parseFloat(this.cp.latitude), lng: parseFloat(this.cp.longitude) },
          origin: { lat: pos.lat, lng: pos.lng },
          travelMode: google.maps.TravelMode.DRIVING
        };
  
        const directionsService = new google.maps.DirectionsService();
        directionsService.route(updatedRequest, (response, status) => {
          if (status === google.maps.DirectionsStatus.OK) {
            directionsRenderer.setDirections(response);
          } else {
            console.error('Error fetching updated directions:', status);
          }
        });
      }, () => {
        console.error('Error: The Geolocation service failed.');
      });
    } else {
      console.error('Error: Your browser doesn\'t support geolocation.');
    }
  }

  setMap() {
    console.info('End Journey')
    if (this.destinationOptions.includes(this.cp.address)) {
      
    }
  }


}
