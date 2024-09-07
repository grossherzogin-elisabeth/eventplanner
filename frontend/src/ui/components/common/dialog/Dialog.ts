export interface Dialog<P = void, T = void, E = void> {
    /**
     * Open this dialog, returning a promise that is resolve when it is closed
     */
    open(param?: P): Promise<T>;

    /**
     * Close this dialog, resulting in the rejection of the open promise
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
