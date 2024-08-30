import type { DateDiff } from './DateDiff';

/**
 * A collection of utility functions for js dates
 */
export class DateUtils {
    public static add(date: Date | number, diff: DateDiff): Date {
        const time = new Date(date);
        const year = time.getFullYear() + (diff.years || 0);
        const month = time.getMonth() + (diff.months || 0);
        const day = time.getDate() + (diff.days || 0);
        const hours = time.getHours() + (diff.hours || 0);
        const minutes = time.getMinutes() + (diff.minutes || 0);
        const seconds = time.getSeconds() + (diff.seconds || 0);
        return new Date(year, month, day, hours, minutes, seconds);
    }

    public static subtract(date: Date | number, diff: DateDiff): Date {
        const time = new Date(date);
        const year = time.getFullYear() - (diff.years || 0);
        const month = time.getMonth() - (diff.months || 0);
        const day = time.getDate() - (diff.days || 0);
        const hours = time.getHours() - (diff.hours || 0);
        const minutes = time.getMinutes() - (diff.minutes || 0);
        const seconds = time.getSeconds() - (diff.seconds || 0);
        return new Date(year, month, day, hours, minutes, seconds);
    }

    public static cropToPrecision(
        date: Date,
        precision: 'years' | 'months' | 'days' | 'hours' | 'minutes' | 'seconds'
    ): Date {
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
                return new Date(
                    date.getFullYear(),
                    date.getMonth(),
                    date.getDate(),
                    date.getHours(),
                    date.getMinutes(),
                    0
                );
            case 'seconds':
                return new Date(
                    date.getFullYear(),
                    date.getMonth(),
                    date.getDate(),
                    date.getHours(),
                    date.getMinutes(),
                    date.getSeconds()
                );
            default:
                return date;
        }
    }
}
