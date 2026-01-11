import type { ValidateFunc } from '@/common/validation/Validator.ts';

export function minLength(length: number, message?: string): ValidateFunc<string> {
    return (value: string | null | undefined) => {
        if (!value || value.length > length) {
            return undefined;
        }
        return message ?? `generic.validation.min-length:${length}`;
    };
}

export function maxLength(length: number, message?: string): ValidateFunc<string> {
    return (value: string | null | undefined) => {
        if (!value || value.length <= length) {
            return undefined;
        }
        return message ?? `generic.validation.max-length:${length}`;
    };
}

export function matchesPattern(pattern: RegExp, message?: string): ValidateFunc<string> {
    return (value: string | null | undefined) => {
        if (!value || pattern.test(value)) {
            return undefined;
        }
        return message ?? 'generic.validation.invalid';
    };
}

export function doesNotContain(forbidden: string | null | undefined, message?: string): ValidateFunc<string> {
    return (value: string | null | undefined) => {
        if (!value || !forbidden || !value.includes(forbidden)) {
            return undefined;
        }
        return message ?? 'generic.validation.forbidden';
    };
}

export function notContainedIn(blacklist: string[], message?: string): ValidateFunc<string> {
    return (value: string | null | undefined) => {
        if (!value || !blacklist.includes(value)) {
            return undefined;
        }
        return message ?? 'generic.validation.forbidden';
    };
}
