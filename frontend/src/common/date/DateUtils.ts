import type { DateDiff } from './DateDiff';

export function addToDate(date: Date | number, diff: DateDiff): Date {
    const time = new Date(date);
    const year = time.getFullYear() + (diff.years || 0);
    const month = time.getMonth() + (diff.months || 0);
    const day = time.getDate() + (diff.days || 0);
    const hours = time.getHours() + (diff.hours || 0);
    const minutes = time.getMinutes() + (diff.minutes || 0);
    const seconds = time.getSeconds() + (diff.seconds || 0);
    return new Date(year, month, day, hours, minutes, seconds);
}

export function subtractFromDate(date: Date | number, diff: DateDiff): Date {
    const time = new Date(date);
    const year = time.getFullYear() - (diff.years || 0);
    const month = time.getMonth() - (diff.months || 0);
    const day = time.getDate() - (diff.days || 0);
    const hours = time.getHours() - (diff.hours || 0);
    const minutes = time.getMinutes() - (diff.minutes || 0);
    const seconds = time.getSeconds() - (diff.seconds || 0);
    return new Date(year, month, day, hours, minutes, seconds);
}

export function updateDate(target: Date | undefined, date: Date): Date {
    const result = target ? new Date(target) : new Date();
    result.setFullYear(date.getFullYear(), date.getMonth(), date.getDate());
    return result;
}

export function updateTime(target: Date | undefined, time: Date, precision: 'hours' | 'minutes' | 'seconds' | 'millis'): Date {
    const result = target ? new Date(target) : new Date();
    switch (precision) {
        case 'hours':
            result.setHours(time.getHours(), 0, 0, 0);
            break;
        case 'minutes':
            result.setHours(time.getHours(), time.getMinutes(), 0, 0);
            break;
        case 'seconds':
            result.setHours(time.getHours(), time.getMinutes(), time.getSeconds(), 0);
            break;
        case 'millis':
            result.setHours(time.getHours(), time.getMinutes(), time.getSeconds(), time.getMilliseconds());
            break;
    }
    return result;
}

export function cropToPrecision(date: Date, precision: 'years' | 'months' | 'days' | 'hours' | 'minutes' | 'seconds'): Date {
    switch (precision) {
        case 'years':
            return new Date(date.getFullYear(), 0, 1, 0, 0, 0);
        case 'months':
            return new Date(date.getFullYear(), date.getMonth(), 1, 0, 0, 0);
        case 'days':
            return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);
        case 'hours':
            return new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), 0, 0);
        case 'minutes':
            return new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), 0);
        case 'seconds':
            return new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
        default:
            return date;
    }
}

export function deserializeDate(date: string): Date {
    // js cannot parse an ISO date time like 2024-06-25T00:00+02:00[Europe/Berlin]
    if (date.includes('[')) {
        return new Date(date.substring(0, date.indexOf('[')));
    }
    return new Date(date);
}
