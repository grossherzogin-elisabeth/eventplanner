import type { MockInstance } from 'vitest';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse, http } from 'msw';
import type { ErrorHandlingService } from '@/application/services';
import { useErrorHandlingService } from '@/application/services';
import type { ErrorDetails } from '@/application/services/ErrorHandlingService';
import { server } from '~/mocks';

describe('ErrorHandlingService', () => {
    let testee: ErrorHandlingService;
    let errorHandlerFunc: MockInstance;

    beforeEach(() => {
        testee = useErrorHandlingService();
        const errorHandler = {
            handle(_details: ErrorDetails): void {},
        };
        errorHandlerFunc = vi.spyOn(errorHandler, 'handle');
        testee.registerErrorHandler(errorHandler.handle);
    });

    it('should construct instance with useErrorHandlingService', () => {
        expect(testee).toBeDefined();
    });

    it('should forward error to error handler', () => {
        const error: ErrorDetails = {
            title: 'title',
            message: 'message',
        };
        testee.handleError(error);
        expect(errorHandlerFunc).toHaveBeenCalledWith(error);
    });

    it('should rethrow errors', async () => {
        server.use(http.get('/api/v1/error', () => HttpResponse.error()));
        let thrown: unknown | undefined;
        try {
            testee.handleRawError(new Error('Test error'));
        } catch (e) {
            thrown = e;
        }
        expect(thrown).toBeDefined();
    });

    it('should detect offline mode', async () => {
        server.use(http.get('/api/v1/error', () => HttpResponse.error()));
        try {
            await fetch('/api/v1/error');
        } catch (e) {
            try {
                testee.handleRawError(e);
            } catch (e) {
                // ignore rethrown error
            }
        }
        expect(errorHandlerFunc).toHaveBeenCalled();
    });

    it('should detect service unavailable', async () => {
        server.use(http.get('/api/v1/test', () => HttpResponse.text('', { status: 503 })));
        const response = await fetch('/api/v1/test');
        try {
            testee.handleRawError(response);
        } catch (e) {
            // ignore rethrown error
        }

        expect(errorHandlerFunc).toHaveBeenCalledWith(
            expect.objectContaining({
                title: 'Server nicht erreichbar',
                message: 'Der Server ist aktuell nicht erreichbar. Bitte versuche es später erneut.',
            })
        );
    });
});
