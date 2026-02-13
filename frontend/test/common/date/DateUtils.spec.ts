import { describe, expect, it } from 'vitest';
import {
    Month,
    addToDate,
    cropToPrecision,
    deserializeDate,
    getDaysOfMonth,
    isSameDate,
    subtractFromDate,
    toIsoDateString,
    updateDate,
    updateTime,
} from '@/common/date';

describe('DateUtils', () => {
    describe('addToDate', () => {
        it('should increase minute', () => {
            const date = new Date(2025, 10, 15, 22, 59);
            const expected = new Date(2025, 10, 15, 23, 0);
            const result = addToDate(date, { minutes: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase hour', () => {
            const date = new Date(2025, 10, 15, 22, 59);
            const expected = new Date(2025, 10, 15, 23, 59);
            const result = addToDate(date, { hours: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase day', () => {
            const date = new Date(2025, 10, 15, 23, 59);
            const expected = new Date(2025, 10, 16, 23, 59);
            const result = addToDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase month', () => {
            const date = new Date(2025, 10, 15, 23, 59);
            const expected = new Date(2025, 11, 15, 23, 59);
            const result = addToDate(date, { months: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase year', () => {
            const date = new Date(2025, 10, 15, 23, 59);
            const expected = new Date(2026, 10, 15, 23, 59);
            const result = addToDate(date, { years: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase day with overflow on month ', () => {
            const date = new Date(2025, 10, 30, 23, 59);
            const expected = new Date(2025, 11, 1, 23, 59);
            const result = addToDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });

        it('should increase day with overflow on month and year ', () => {
            const date = new Date(2025, 11, 31, 23, 59);
            const expected = new Date(2026, 0, 1, 23, 59);
            const result = addToDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });
    });

    describe('subtractFromDate', () => {
        it('should decrease minute', () => {
            const date = new Date(2025, 10, 15, 23, 0);
            const expected = new Date(2025, 10, 15, 22, 59);
            const result = subtractFromDate(date, { minutes: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease hour', () => {
            const date = new Date(2025, 10, 15, 23, 59);
            const expected = new Date(2025, 10, 15, 22, 59);
            const result = subtractFromDate(date, { hours: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease day', () => {
            const date = new Date(2025, 10, 16, 23, 59);
            const expected = new Date(2025, 10, 15, 23, 59);
            const result = subtractFromDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease month', () => {
            const date = new Date(2025, 11, 15, 23, 59);
            const expected = new Date(2025, 10, 15, 23, 59);
            const result = subtractFromDate(date, { months: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease year', () => {
            const date = new Date(2026, 10, 15, 23, 59);
            const expected = new Date(2025, 10, 15, 23, 59);
            const result = subtractFromDate(date, { years: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease day with overflow on month ', () => {
            const date = new Date(2025, 11, 1, 23, 59);
            const expected = new Date(2025, 10, 30, 23, 59);
            const result = subtractFromDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });

        it('should decrease day with overflow on month and year ', () => {
            const date = new Date(2026, 0, 1, 23, 59);
            const expected = new Date(2025, 11, 31, 23, 59);
            const result = subtractFromDate(date, { days: 1 });
            expect(result).toEqual(expected);
        });
    });

    describe('isSameDate', () => {
        it('should return true', () => {
            const a = new Date(2025, 10, 15, 23, 59);
            const b = new Date(2025, 10, 15, 0, 0);
            expect(isSameDate(a, b)).toBeTruthy();
        });

        it('should return false', () => {
            const a = new Date(2025, 10, 15, 23, 59);
            const b = new Date(2025, 10, 16, 0, 0);
            expect(isSameDate(a, b)).toBeFalsy();
        });
    });

    describe('getDaysOfMonth', () => {
        it.each([
            [Month.JANUARY, 31],
            [Month.FEBRUARY, 28],
            [Month.MARCH, 31],
            [Month.APRIL, 30],
            [Month.MAY, 31],
            [Month.JUNE, 30],
            [Month.JULY, 31],
            [Month.AUGUST, 31],
            [Month.SEPTEMBER, 30],
            [Month.OCTOBER, 31],
            [Month.NOVEMBER, 30],
            [Month.DECEMBER, 31],
        ])('should return correct days for month $0 in non leap year', (month, days) => {
            const date = new Date(2025, month, 15);
            expect(getDaysOfMonth(date)).toEqual(days);
        });

        it.each([2000, 2024, 2028])('should return 29 days for February in leap year', (year) => {
            const date = new Date(year, 1, 15);
            expect(getDaysOfMonth(date)).toEqual(29);
        });
    });

    describe('updateDate', () => {
        it('should only change the date', () => {
            const date = new Date(2025, 10, 15, 23, 59);
            const expected = new Date(2024, 3, 12, 23, 59);
            const result = updateDate(date, new Date(2024, 3, 12, 10, 15));
            expect(result).toEqual(expected);
        });
    });

    describe('updateTime', () => {
        it('should update full time', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 14, 15, 16, 17);
            const result = updateTime(date, new Date(2024, 3, 12, 14, 15, 16, 17));
            expect(result).toEqual(expected);
        });

        it('should crop to seconds', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 14, 15, 16, 0);
            const result = updateTime(date, new Date(2024, 3, 12, 14, 15, 16, 17), 'seconds');
            expect(result).toEqual(expected);
        });

        it('should crop to minutes', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 14, 15, 0, 0);
            const result = updateTime(date, new Date(2024, 3, 12, 14, 15, 16, 17), 'minutes');
            expect(result).toEqual(expected);
        });

        it('should crop to hours', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 14, 0, 0, 0);
            const result = updateTime(date, new Date(2024, 3, 12, 14, 15, 16, 17), 'hours');
            expect(result).toEqual(expected);
        });
    });

    describe('cropToPrecision', () => {
        it('should crop to seconds', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 10, 11, 12, 0);
            const result = cropToPrecision(date, 'seconds');
            expect(result).toEqual(expected);
        });

        it('should crop to minutes', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 10, 11, 0, 0);
            const result = cropToPrecision(date, 'minutes');
            expect(result).toEqual(expected);
        });

        it('should crop to hours', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 10, 0, 0, 0);
            const result = cropToPrecision(date, 'hours');
            expect(result).toEqual(expected);
        });

        it('should crop to days', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 15, 0, 0, 0, 0);
            const result = cropToPrecision(date, 'days');
            expect(result).toEqual(expected);
        });

        it('should crop to month', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 10, 1, 0, 0, 0, 0);
            const result = cropToPrecision(date, 'months');
            expect(result).toEqual(expected);
        });

        it('should crop to year', () => {
            const date = new Date(2025, 10, 15, 10, 11, 12, 13);
            const expected = new Date(2025, 0, 1, 0, 0, 0, 0);
            const result = cropToPrecision(date, 'years');
            expect(result).toEqual(expected);
        });
    });

    describe('deserializeDate', () => {
        it('should deserialize date with timezone name correctly', () => {
            const expected = new Date(2024, 5, 25, 10, 15);
            const result = deserializeDate('2024-06-25T10:15+02:00[Europe/Berlin]');
            expect(result).toEqual(expected);
        });

        it('should deserialize date without timezone name correctly correctly', () => {
            const expected = new Date(2024, 5, 25, 10, 15);
            const result = deserializeDate('2024-06-25T10:15+02:00');
            expect(result).toEqual(expected);
        });

        it('should deserialize UTC date correctly', () => {
            const expected = new Date(2024, 5, 25, 10, 15);
            const result = deserializeDate('2024-06-25T08:15Z');
            expect(result).toEqual(expected);
        });

        it('should deserialize date', () => {
            const expected = new Date(2024, 5, 25);
            const result = deserializeDate('2024-06-25');
            expect(result).toEqual(expected);
        });
    });

    describe('toIsoDateString', () => {
        it('should serialize date', () => {
            const expected = '2024-06-25';
            const result = toIsoDateString(new Date(2024, 5, 25, 10, 15));
            expect(result).toEqual(expected);
        });

        it('should serialize date without time', () => {
            const expected = '2024-06-25';
            const result = toIsoDateString(new Date(2024, 5, 25));
            expect(result).toEqual(expected);
        });
    });
});
