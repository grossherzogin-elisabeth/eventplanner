import { filterUndefined } from '@/common';

export type ValidateFunc<T> = (value: T | null | undefined) => string | undefined;

export class Validator {
    private errors: Record<string, string[]> = {};

    public static validate<T>(name: string, value: T | null | undefined, ...validators: ValidateFunc<T>[]): Validator {
        return new Validator().validate(name, value, ...validators);
    }

    public validate<T>(name: string, value: T | null | undefined, ...validators: ValidateFunc<T>[]): Validator {
        const result = validators.map((f) => f(value)).filter(filterUndefined);
        if (result.length > 0) {
            if (this.errors[name]) {
                result.forEach((err) => this.errors[name].push(err));
            } else {
                this.errors[name] = result;
            }
        }
        return this;
    }

    public getErrors(): Record<string, string[]> {
        return this.errors;
    }
}

export function notEmpty(message?: string): ValidateFunc<unknown> {
    return (value: unknown) => {
        if (value !== undefined && value !== null && value !== '') {
            return undefined;
        }
        return message ?? 'generic.validation.required';
    };
}
