export interface Sheet<P = void, T = void, E = void> {
    /**
     * Open this sheet, returning a promise that is resolve when it is closed
     */
    open(param?: P, onSubmit?: (result: T) => Promise<unknown>): Promise<T>;

    /**
     * Close this sheet, resulting in the rejection of the open promise
     */
    close(): void;

    /**
     * Submit an optional positive result and close the dialog resolving the promise
     */
    submit(result?: T): void;

    /**
     * Submit an optional negative result and close the dialog rejecting the promise
     */
    reject(reason?: E): void;
}
