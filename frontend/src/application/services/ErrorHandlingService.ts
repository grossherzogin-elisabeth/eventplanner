import type { AccountRepository } from '@/application/ports';







export interface ErrorDetails {
    title?: string;
    message?: string;
    error?: unknown | Error | Response;
    retryText?: string;
    retry?: () => unknown;
    cancelText?: string;
}

export class ErrorHandlingService {
    private readonly accountRepository: AccountRepository;

    public constructor(params: { accountRepository: AccountRepository }) {
        console.log('🚀 Initializing ErrorHandlingService');
        this.accountRepository = params.accountRepository;
    }

    private errorHandler: (error: ErrorDetails) => void = (error: ErrorDetails) => {
        alert(error.message || error.error);
        console.log(error.error);
    };

    public registerErrorHandler(handler: (error: ErrorDetails) => void): void {
        this.errorHandler = handler;
    }

    public handleError(error: ErrorDetails): void {
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
            const response = e as Response;
            if (response.status === 401) {
                this.accountRepository.login(location.pathname);
            } else if (response.status === 502 || response.status === 503) {
                this.handleError({
                    title: 'Server nicht erreichbar',
                    message: 'Der Server ist aktuell nicht erreichbar. Bitte versuche es später erneut.',
                    error: e,
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
            this.handleError({ error: e });
        }

        // throw again for the async button to handle error state
        throw e;
    }
}
