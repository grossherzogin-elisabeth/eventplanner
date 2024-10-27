export interface ErrorDetails {
    title?: string;
    message?: string;
    error?: unknown | Error | Response;
    retry?: () => void;
}

export class ErrorHandlingService {
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
        this.handleError({
            error: e,
        });

        // throw again for the async button to handle error state
        throw e;
    }

    public handleUnexpectedError(e: unknown | Error | Response): void {
        this.handleError({
            error: e,
        });

        // throw again for the async button to handle error state
        throw e;
    }
}
