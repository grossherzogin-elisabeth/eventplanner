import type { MockInstance } from 'vitest';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse, http } from 'msw';
import type { AccountRepository } from '@/application/ports';
import type { ErrorHandlingService } from '@/application/services';
import { ErrorHandlingService as ErrorHandlingServiceImpl } from '@/application/services';
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

    it('triggers login for unauthorized responses', () => {
        const accountRepository: AccountRepository = {
            getAccount: vi.fn(),
            login: vi.fn(async () => undefined),
            logout: vi.fn(async () => undefined),
        };
        const service = new ErrorHandlingServiceImpl({ accountRepository });

        try {
            service.handleRawError(new Response('', { status: 401 }));
        } catch {
            // ignore rethrow
        }

        expect(accountRepository.login).toHaveBeenCalledWith(location.pathname);
    });

    it('forwards unknown response errors to the registered handler', () => {
        const accountRepository: AccountRepository = {
            getAccount: vi.fn(),
            login: vi.fn(async () => undefined),
            logout: vi.fn(async () => undefined),
        };
        const service = new ErrorHandlingServiceImpl({ accountRepository });
        const handler = vi.fn();
        service.registerErrorHandler(handler);

        const response = new Response('', { status: 418 });
        try {
            service.handleRawError(response);
        } catch {
            // ignore rethrow
        }

        expect(handler).toHaveBeenCalledWith(expect.objectContaining({ error: response }));
    });
});
