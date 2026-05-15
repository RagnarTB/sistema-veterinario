import { AbstractControl, ValidationErrors } from '@angular/forms';

export class CustomValidators {
  static dni(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const regex = /^[0-9]{8}$/;
    return regex.test(control.value) ? null : { dni: true };
  }

  static telefono(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const regex = /^[0-9]{9}$/;
    return regex.test(control.value) ? null : { telefono: true };
  }

  static ruc(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const regex = /^[0-9]{11}$/;
    return regex.test(control.value) ? null : { ruc: true };
  }

  static noWhitespace(control: AbstractControl): ValidationErrors | null {
    if (typeof control.value === 'string') {
      const isWhitespace = control.value.trim().length === 0;
      const isValid = !isWhitespace;
      return isValid ? null : { whitespace: true };
    }
    return null;
  }
}
