import { Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms'
import { AuthService } from '../services/auth.service';
import { StorageService } from '../services/storage.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm! : FormGroup
  isLoggedIn = false
  isLoginFailed = false
  errorMessage = ''
  roles : string[] = []

  constructor(private fb : FormBuilder, private authSvc: AuthService, private storageSvc : StorageService, private router : Router) {}

  ngOnInit(): void {
    this.loginForm = this.createForm()
  }

  createForm() {
    return this.fb.group({
      username : this.fb.control<string>('Username', [Validators.required, Validators.min(3)]),
      password: this.fb.control<string>('Password', [Validators.required, Validators.min(8)])
    })
  }

  submit() {
    const username = this.loginForm.value.username
    const password = this.loginForm.value.password
    this.authSvc.login(username, password).subscribe({
      next:data => {
        console.info(data)
        this.storageSvc.saveUser(data)

        // this.isLoginFailed = false
        // this.isLoggedIn = true
        // this.roles = this.storageSvc.getUser().roles
        // this.reloadPage()
        this.router.navigate(['/search'])
      },
      error:err => {
        this.errorMessage = err.errorMessage
        this.isLoginFailed = true
      }
    })


  }

  reloadPage() : void {
    window.location.reload()
  }
}
