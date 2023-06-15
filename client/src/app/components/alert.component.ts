import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent {

  @Output() toDismiss: EventEmitter<void> = new EventEmitter<void>()

  dismiss() : void {
    // Emit the "dismiss" event when the alert is dismissed
    this.toDismiss.emit()
  }
}
