import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { SearchService } from '../services/search.service';
import { ActivatedRoute } from '@angular/router';
import { ResultsDataSource } from '../model/ResultsDataSource';
import { MatSort } from '@angular/material/sort';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { map, Subscription } from 'rxjs';

@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['./display.component.css']
})
export class DisplayComponent implements OnInit, AfterViewInit {
  id! : string
  destination! : string
  dataSource! : ResultsDataSource
  displayedColumns = ["carParkId", "address", "lotsAvailable", "distance", "cost"]
  input! : FormGroup
  sub$! : Subscription

  @ViewChild(MatSort)
  sort! : MatSort

  constructor(private searchSvc : SearchService, private activatedRoute : ActivatedRoute, private fb : FormBuilder) {
  }

  ngOnInit(): void {
    this.id = this.activatedRoute.snapshot.params['id']
    this.destination = this.activatedRoute.snapshot.params['destination']
    
    this.dataSource = new ResultsDataSource(this.searchSvc)
    this.dataSource.loadResults(this.id)

    this.input = this.fb.group({
      inputDist : this.fb.control('', [Validators.required, Validators.min(200), Validators.max(1000)])
    })
  }

  onRowClicked(row : any) {
    console.info('Row is clicked: ',row)
  }

  changeDistance() {

  }

  ngAfterViewInit(): void {
      this.sort.sortChange.subscribe(() => {
        console.info("sort1");
      const activeSort = this.sort.active;
      this.dataSource.sortResults(activeSort);
      })
  }

  onInputChange() {
    console.log('input value changed: ', this.input.value.inputDist)
    this.dataSource.updateResults(this.id, this.input.value.inputDist)
  }
}
