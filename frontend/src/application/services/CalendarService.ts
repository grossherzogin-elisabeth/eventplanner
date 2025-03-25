import type { Event } from '@/domain';

export class CalendarService {
    public createCalendarEntries(events: Event[]): string {
        return `
            BEGIN:VCALENDAR
                VERSION:2.0
                METHOD:PUBLISH
                ${events.map((e) => this.createIcsEvent(e)).join('\n')}
                ${this.createIcsReminder('1W')}
            END:VCALENDAR
        `
            .split('\n')
            .map((l) => l.trim())
            .filter((l) => l.length > 0)
            .join('\n');
    }

    private createIcsEvent(event: Event): string {
        return `
            BEGIN:VEVENT
                UID:${event.key}@großherzogin-elisabeth.de
                LOCATION:${event.locations[0]?.address || event.locations[0]?.name}
                DTSTAMP:${this.formatIcsDate(new Date())}
                ORGANIZER;CN=Grossherzogin Elisabeth e.V.:MAILTO:office@grossherzogin-elisabeth.de
                DTSTART:${this.formatIcsDate(event.start)}
                DTEND:${this.formatIcsDate(event.end)}
                SUMMARY:${event.name}
                DESCRIPTION:${this.createEventDescription(event)}
            END:VEVENT
        `
            .split('\n')
            .map((l) => l.trim())
            .join('\n');
    }

    private createEventDescription(event: Event): string {
        const description = event.description;
        const link = `${window.location.origin}/events/${event.start.getFullYear()}/details/${event.key}`;
        return description + '\\n' + link;
    }

    private createIcsReminder(t: string = '1W'): string {
        return `
            BEGIN:VALARM
                DESCRIPTION:REMINDER
                TRIGGER;RELATED=START:-P${t}
                ACTION:DISPLAY
            END:VALARM
        `
            .split('\n')
            .map((l) => l.trim())
            .join('\n');
    }

    private formatIcsDate(date: Date): string {
        const year = String(date.getFullYear());
        let month = String(date.getMonth() + 1);
        if (month.length === 1) month = `0${month}`;
        let day = String(date.getDate());
        if (day.length === 1) day = `0${day}`;
        let hour = String(date.getHours());
        if (hour.length === 1) hour = `0${hour}`;
        let minute = String(date.getMinutes());
        if (minute.length === 1) minute = `0${minute}`;
        return `${year}${month}${day}T${hour}${minute}00`;
    }
}
