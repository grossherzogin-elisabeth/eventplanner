import type { AccountRepository, ErrorReportingRepository } from '@/application/ports';

export interface ErrorDetails {
    title?: string;
    message?: string;
    error?: Error | Response;
    retryText?: string;
    retry?: () => unknown;
    cancelText?: string;
}

export class ErrorHandlingService {
    private readonly accountRepository: AccountRepository;
    private readonly errorReportingRepository: ErrorReportingRepository;

    public constructor(params: { accountRepository: AccountRepository; errorReportingRepository: ErrorReportingRepository }) {
        this.accountRepository = params.accountRepository;
        this.errorReportingRepository = params.errorReportingRepository;
        window.addEventListener('error', (event) => {
            this.report(
                event.message || 'Unhandled window error',
                'window',
                event.error ?? `${event.filename}:${event.lineno}:${event.colno}`
            );
        });
    }

    private errorHandler: (error: ErrorDetails) => void = (error: ErrorDetails) => {
        alert(error.message || error.error);
        console.log(error.error);
    };

    public registerErrorHandler(handler: (error: ErrorDetails) => void): void {
        this.errorHandler = handler;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public report(message: string, component: string, e?: any): void {
        if (e instanceof Response) {
            // don't report errors that come from an unsuccessful request
            return;
        }
        console.log('📢 Reporting error');
        this.errorReportingRepository.report(message, component, e?.stack || e?.message || e?.toString());
    }

    public handleError(error: ErrorDetails): void {
        this.report(error.title || 'Frontend encountered an error', 'ErrorHandlingService', error.error);
        if (this.errorHandler) {
            this.errorHandler(error);
        }
        if (error.error) {
            // throw again for the async button to handle error state
            throw error.error;
        }
    }

    public handleRawError(e: unknown | Error | Response): void {
        if (e instanceof Response) {
            const response = e;
            if (response.status === 401) {
                this.accountRepository.login(location.pathname);
            } else if (response.status === 502 || response.status === 503) {
                this.handleError({
                    title: 'Server nicht erreichbar',
                    message: 'Der Server ist aktuell nicht erreichbar. Bitte versuche es später erneut.',
                    error: response,
                });
            } else {
                this.handleError({ error: e });
            }
        } else if (e instanceof Error && e.message === 'Failed to fetch') {
            this.handleError({
                title: 'Funktion nicht verfügbar',
                message:
                    'Du scheinst gerade offline zu sein. Diese Funktion ist im offline Modus nicht verfügbar. Bitte prüfe deine Internet Verbindung und versuche es erneut.',
                error: e,
            });
        } else {
            this.handleError({ error: e instanceof Error || e instanceof Response ? e : undefined });
        }

        // throw again for the async button to handle error state
        throw e;
    }
}
