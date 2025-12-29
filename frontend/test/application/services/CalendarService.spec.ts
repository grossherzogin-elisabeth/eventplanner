import { describe, expect, it } from 'vitest';
import { useCalendarService } from '@/application/services';

describe('CalendarService', () => {
    it('should construct instance with useCalendarService', () => {
        const testee = useCalendarService();
        expect(testee).toBeDefined();
    });
});
