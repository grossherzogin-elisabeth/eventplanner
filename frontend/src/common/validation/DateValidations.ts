import type { ValidateFunc } from '@/common/validation/Validator.ts';

export function before(other: Date | null | undefined, message?: string): ValidateFunc<Date> {
    return (value: Date | null | undefined) => {
        if (!value || !other || value.getTime() < other.getTime()) {
            return undefined;
        }
        return message ?? `generic.validation.before|date(${other.toISOString()})`;
    };
}

export function after(other: Date | null | undefined, message?: string): ValidateFunc<Date> {
    return (value: Date | null | undefined) => {
        if (!value || !other || value.getTime() > other.getTime()) {
            return undefined;
        }
        return message ?? `generic.validation.after|date(${other.toISOString()})`;
    };
}
