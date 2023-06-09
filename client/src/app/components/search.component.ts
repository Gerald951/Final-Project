import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { dateComparisonValidator, generate8RandomChar } from './utils';
import { Destination } from '../model/destination';
import { SearchService } from '../services/search.service';
import { map, Subscription } from 'rxjs';
import { Carpark } from '../model/carpark';
import { HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  destinationOptions: string[] = ["Harbourfront Centre", "Resorts World Sentosa", "VivoCity P2", "VivoCity P3", "Sentosa", "Westgate", "IMM Building", "JCube", "National Gallery", "Singapore Flyer", 
  "Millenia Singapore", "The Esplanade", "Raffles City", "Marina Square", "Suntec City", "Marina Bay Sands", "Centrepoint", "Cineleisure", "Orchard Point", "Concorde Hotel", "Plaza Singapura",
  "The Cathay", "Mandarin Hotel", "Wisma Atria", "The Heeren", "Ngee Ann City", "Orchard Central", "Wheelock Place", "Orchard Gateway", "Tang Plaza", "Far East Plaza", "Paragon",
  "313@Somerset", "The Atrium@Orchard", "Bukit Panjang Plaza", "Clarke Quay", "The Star Vista", "Funan Mall", "Lot One", "Tampines Mall", "Junction 8", "Bedok Mall", "Bugis+"];
  listOfCP : Carpark[] = []
  sub$! : Subscription

  constructor(private fb : FormBuilder, private searchSvc : SearchService, private router : Router) {}

  ngOnInit(): void {
      this.form = this.fb.group({
        destination : this.fb.control('', Validators.required),
        dateIn: this.fb.control('', Validators.required),
        hourIn: this.fb.control('', Validators.required),
        dateOut: this.fb.control('', Validators.required),
        hourOut: this.fb.control('', Validators.required)
      }, { validators: dateComparisonValidator() })
  }

  
  submit() {
    const submitted = this.form.value

    const year1 = submitted.dateIn.toDateString().substring(11,15)
    const month1 = this.getMonth(submitted.dateIn.toDateString().substring(4,7))
    const day1 = submitted.dateIn.toDateString().substring(8,10)
    const year2 = submitted.dateOut.toDateString().substring(11,15)
    const month2 = this.getMonth(submitted.dateOut.toDateString().substring(4,7))
    const day2 = submitted.dateOut.toDateString().substring(8,10)
    
    const dateInValue = new Date(year1, month1, day1)
    const dateOutValue = new Date(year2, month2, day2)
    const diffInMillis = Math.abs(dateOutValue.getTime() - dateInValue.getTime())
    const diffInDays = Math.ceil(diffInMillis / (1000*60*60*24))
    console.info(diffInDays)

    // get dayOfWeek
    const dateInStr = submitted.dateIn.toDateString()
    const firstDay = dateInStr.substring(0,3)
    const firstDayInt = this.getDayOfWeeksInt(firstDay)

    const lastDayInt = firstDayInt + diffInDays
    const lastDay = this.getDayOfWeeks(lastDayInt)

    // get HourIn and HourOut
    let hourIn = submitted.hourIn 
    let hourInStr = hourIn + ":00"
    console.info(hourInStr)
    let hourOut = submitted.hourOut
    let hourOutStr = hourOut + ":00"
    console.info(hourOutStr)

    const destination : Destination = {
      id : generate8RandomChar(),
      destination : submitted.destination,
      distance : 200,
      listOfParkedTime : [],
      listOfExitTime : [],
      dayOfWeek : [],
    }
    
    
    if (diffInDays > 0) {
      let endTime = "23:59:59"
      let startTime = "00:00:00"

      for (let i = 0; i<diffInDays+1; i++) {
        if (i == 0) {
          destination.listOfParkedTime.push(hourInStr)
          destination.listOfExitTime.push(endTime)
          destination.dayOfWeek.push(firstDay)
          console.info(firstDay)
        } else if (i == diffInDays) {
          destination.listOfParkedTime.push(startTime)
          destination.listOfExitTime.push(hourOutStr)
          destination.dayOfWeek.push(lastDay)
          console.info(lastDay)
        } else {
          destination.listOfParkedTime.push(startTime)
          destination.listOfExitTime.push(endTime)

          const dayInt = firstDayInt + i
          const day = this.getDayOfWeeks(dayInt)
          destination.dayOfWeek.push(day)
          console.info(day)
        }
      }

    } else {
      destination.listOfParkedTime.push(submitted.hourIn += ":00")
      destination.listOfExitTime.push(submitted.hourOut += ":00")
      destination.dayOfWeek.push(firstDay)
    }

    console.info(destination)

    this.sub$ = this.searchSvc.searchNearbyCarParks(destination).subscribe(
      (response: HttpResponse<string>) => {
        if (response.status === 200) {
          // Successful response with status code 200
          console.log('Request succeeded with status code 200');
          this.router.navigate(['/display', destination.id, destination.destination])
        } else {
          // Handle other status codes
          console.log('Request returned status code:', response.status);
        }
      },
      (error) => {
        // Handle error
        console.error('An error occurred:', error);
      }
    );
}

  ngOnDestroy(): void {
      this.sub$.unsubscribe()
  }

  getMonth(month : string) : number {
    switch(month) {
      case "Jan":
        return 0;
      case "Feb":
        return 1;
      case "Mar":
        return 2;
      case "Apr":
        return 3;
      case "May":
        return 4
      case "Jun":
        return 5
      case "Jul":
        return 6
      case "Aug":
        return 7
      case "Sep":
        return 8
      case "Oct":
        return 9
      case "Nov":
        return 10
      default:
        return 11
    }
  }

  getDayOfWeeks(day : number) : string {
    switch(day) {
      case 1:
        return "Mon";
      case 2:
        return "Tue";
      case 3:
        return "Wed";
      case 4:
        return "Thu";
      case 5:
        return "Fri";
      case 6:
        return "Sat";
      default:
        return "Sun";
    }
  }

  getDayOfWeeksInt(day : string) : number {
    switch(day) {
      case "Mon":
        return 1;
      case "Tue":
        return 2;
      case "Wed":
        return 3;
      case "Thu":
        return 4;
      case "Fri":
        return 5;
      case "Sat":
        return 6;
      default:
        return 7;
    }
  }
  
}

