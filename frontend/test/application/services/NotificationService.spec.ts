import { describe, expect, it } from 'vitest';
import { useNotificationService } from '@/application/services';

describe('NotificationService', () => {
    it('should construct instance with useNotificationService', () => {
        const testee = useNotificationService();
        expect(testee).toBeDefined();
    });
});
