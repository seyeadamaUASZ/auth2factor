import {Component, OnInit} from '@angular/core';
import {Auth2factorService} from "../service/auth2factor.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import Swal from 'sweetalert2';
import {User} from "../models/user.model";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit{
  ngForm!:FormGroup;
  constructor(private authS:Auth2factorService,private form:FormBuilder,
              private router:Router) {
  }
  ngOnInit(): void {
    this.ngForm = this.form.group({
      firstName:['',Validators.required],
      lastName:['',Validators.required],
      username:['',Validators.required],
      password:['',Validators.required],
      confirmPassword:['',Validators.required],
      mfa:[false]
    })
  }

  submit(){
    console.log('value form '+ JSON.stringify(this.ngForm.value));
    let password = this.ngForm.value.password;
    let confirmPass = this.ngForm.value.confirmPassword;
    if(password ===confirmPass){
        let data : User = this.ngForm.value;
        this.authS.registration(data)
          .subscribe({
            next: resp=>{
              if(resp){
                Swal.fire({
                  icon: 'success',
                  //title: 'Oops...',
                  text: 'Inscription enregitrée avec succès !!',
                })
                this.router.navigate(['login']);
              }else{
                console.log("Error !!")
              }
            },error:err => {
              Swal.fire({
                icon: 'error',
                //title: 'Oops...',
                text: 'Erreur survenue sur l\'inscription',
              })
            }
          })

    }
    else{
        Swal.fire({
          icon: "warning",
          text: "Les mot de passes saisis ne correspondent pas !!!",
        })
    }

  }




}
