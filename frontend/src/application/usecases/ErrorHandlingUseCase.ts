export interface ErrorDetails {
    title?: string;
    message?: string;
    error?: unknown | Error | Response;
    retry?: () => void;
}

export class ErrorHandlingUseCase {
    private errorHandler: (error: ErrorDetails) => void = (error: ErrorDetails) => {
        console.error(error.error);
    };

    public registerErrorHandler(handler: (error: ErrorDetails) => void): void {
        this.errorHandler = handler;
    }

    public handleError(error: ErrorDetails): void {
        console.error(error.error);
        if (this.errorHandler) {
            this.errorHandler(error);
        }
    }

    public handleRawError(e: unknown | Error | Response): void {
        this.handleError({
            error: e,
        });
    }

    public handleUnexpectedError(e: unknown | Error | Response): void {
        this.handleError({
            error: e,
        });
    }
}
