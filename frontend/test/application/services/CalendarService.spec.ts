import { describe, expect, it } from 'vitest';
import { CalendarService } from '@/application/services';
import { mockEvent, mockLocation1 } from '~/mocks';

function asIcsDate(date: Date): string {
    const year = String(date.getFullYear());
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    return `${year}${month}${day}T${hour}${minute}00`;
}

describe('CalendarService', () => {
    it('should create ICS content including event details and reminder block', () => {
        const testee = new CalendarService();

        const event = mockEvent({
            key: 'evt-1',
            name: 'Training Trip',
            description: 'Bring your gear',
            start: new Date('2026-07-09T08:03:00Z'),
            end: new Date('2026-07-10T09:04:00Z'),
            locations: [mockLocation1({ address: 'Harbor Street 1' })],
        });
        const ics = testee.createCalendarEntries([event]);

        expect(ics).toContain('BEGIN:VCALENDAR');
        expect(ics).toContain('BEGIN:VEVENT');
        expect(ics).toContain('UID:evt-1@großherzogin-elisabeth.de');
        expect(ics).toContain('LOCATION:Harbor Street 1');
        expect(ics).toContain('SUMMARY:Training Trip');
        expect(ics).toContain('DESCRIPTION:Bring your gear\\nhttp://localhost:3000/events/2026/details/evt-1');
        expect(ics).toContain('BEGIN:VALARM');
        expect(ics).toContain(`DTSTART:${asIcsDate(event.start)}`);
        expect(ics).toContain(`DTEND:${asIcsDate(event.end)}`);
    });

    it('should fall back to location name when address is missing', () => {
        const testee = new CalendarService();
        const event = mockEvent({
            key: 'evt-2',
            locations: [mockLocation1({ address: undefined, name: 'Bremen Pier' })],
        });

        const ics = testee.createCalendarEntries([event]);
        expect(ics).toContain('LOCATION:Bremen Pier');
    });
});
