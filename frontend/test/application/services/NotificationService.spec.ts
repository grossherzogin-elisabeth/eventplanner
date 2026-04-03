import { describe, expect, it, vi } from 'vitest';
import { NotificationService } from '@/application/services';

describe('NotificationService', () => {
    it('should use alert as default notification handler', () => {
        const testee = new NotificationService();
        const alertSpy = vi.spyOn(globalThis, 'alert').mockImplementation(() => undefined);

        testee.success('saved');

        expect(alertSpy).toHaveBeenCalledWith('saved');
    });

    it('should forward all notification types to registered handler', () => {
        const testee = new NotificationService();
        const handler = vi.fn();
        testee.registerNotificationHandler(handler);

        testee.success('ok');
        testee.info('info');
        testee.warning('warn');
        testee.error('err');

        expect(handler).toHaveBeenNthCalledWith(1, 'ok', 'success');
        expect(handler).toHaveBeenNthCalledWith(2, 'info', 'info');
        expect(handler).toHaveBeenNthCalledWith(3, 'warn', 'warning');
        expect(handler).toHaveBeenNthCalledWith(4, 'err', 'error');
    });
});
