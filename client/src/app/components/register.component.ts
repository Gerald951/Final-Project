import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { checkPassword } from './utils';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm! : FormGroup
  isSuccessful = false
  isSignUpFailed = false
  errorMessage = ''
  hidePassword = true
  hidePassword2 = true


  constructor(private AuthSvc : AuthService, private fb : FormBuilder) {}

  ngOnInit(): void {
      this.registerForm = this.createForm()
  }

  createForm() {
    return this.fb.group({
      username : this.fb.control<string>('Username', [Validators.required, Validators.min(3)]),
      password: this.fb.control<string>('Password', [Validators.required, Validators.min(8)]),
      password2: this.fb.control<string>('Repeat Password', [Validators.required, Validators.min(8)]),
      email: this.fb.control<string>('Email', [Validators.required, Validators.email])
    }, { validators : checkPassword() })
  }

  submit() {
    const {username,password,email} = this.registerForm.value
    this.AuthSvc.register(username,email,password).subscribe({
      next:data => {
        console.log(data)
        this.isSuccessful = true
        this.isSignUpFailed = false
      },
      error:err => {
        this.errorMessage = err.errorMessage
        this.isSignUpFailed = true
      }
    })
  }
}
