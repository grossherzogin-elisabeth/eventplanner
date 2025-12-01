import { useNotificationService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('NotificationService', () => {
    it('should construct instance with useNotificationService', () => {
        const testee = useNotificationService();
        expect(testee).toBeDefined();
    });
});
