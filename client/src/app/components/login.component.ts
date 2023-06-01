import { Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms'
import { LoginService } from '../services/login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm! : FormGroup
  constructor(private fb : FormBuilder, private loginSvc : LoginService) {}

  ngOnInit(): void {
      this.loginForm = this.createForm()
  }

  createForm() {
    return this.fb.group({
      username : this.fb.control<string>('username', [Validators.required, Validators.min(3)]),
      password: this.fb.control<string>('password', [Validators.required, Validators.min(8)])
    })
  }

  submit() {
    
  }
}
