export interface ErrorDetails {
    title?: string;
    message?: string;
    error?: unknown | Error | Response;
    retryText?: string;
    retry?: () => void;
}

export class ErrorHandlingService {
    private readonly login: () => void;

    public constructor(login: () => void) {
        this.login = login;
    }

    private errorHandler: (error: ErrorDetails) => void = (error: ErrorDetails) => {
        alert(error.message || error.error);
        console.log(error.error);
    };

    public registerErrorHandler(handler: (error: ErrorDetails) => void): void {
        this.errorHandler = handler;
    }

    public handleError(error: ErrorDetails): void {
        console.error(error.error || error);
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
                this.login();
            } else {
                this.handleError({ error: e });
            }
        } else {
            this.handleError({ error: e });
        }

        // throw again for the async button to handle error state
        throw e;
    }
}
