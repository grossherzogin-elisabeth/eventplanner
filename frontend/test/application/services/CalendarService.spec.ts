import { useCalendarService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('CalendarService', () => {
    it('should construct instance with useCalendarService', () => {
        const testee = useCalendarService();
        expect(testee).toBeDefined();
    });
});
