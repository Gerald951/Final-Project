import { AbstractControl, FormGroup, ValidationErrors, ValidatorFn } from "@angular/forms";

export function dateComparisonValidator(): ValidatorFn {
    return (control : AbstractControl)  : ValidationErrors | null => {
        const form = control as FormGroup

        const dateInControl = form.get('dateIn')
        const timeInControl = form.get('hourIn')
        const dateOutControl = form.get('dateOut')
        const timeOutControl = form.get('hourOut')
    
        if (dateInControl && timeInControl && dateOutControl && timeOutControl) {
            const dateInValue = new Date(dateInControl.value);
            const timeInValue = timeInControl.value as string;
            const dateOutValue = new Date(dateOutControl.value);
            const timeOutValue = timeOutControl.value as string;
        
            const dateTimeIn : any = new Date(`${dateInValue.toDateString()} ${timeInValue}`);
            const dateTimeOut : any = new Date(`${dateOutValue.toDateString()} ${timeOutValue}`);
        
            if (dateTimeIn <= dateTimeOut) {
                
                return null
            } else {
                return { dateComparison : true }
            }
        }
        return null
    }

  }

  export function checkPassword() : ValidatorFn {
    return (control : AbstractControl) : ValidationErrors | null => {
        const form = control as FormGroup

        const password = form.get('password')
        const repeatPassword = form.get('password2')

        if (password!=null && repeatPassword!=null && password.toString() === repeatPassword.toString()) {
            return null
        } else {
            return { passwordNotEqual : true }
        }
    }
  }

export function generate8RandomChar() : string {
    let result = ''
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let totalChar = characters.length
    let i = 0

    while (i<8) {
        let c = characters.charAt(Math.floor(Math.random() * totalChar))
        result += c
        i++
    }

    return result


}




