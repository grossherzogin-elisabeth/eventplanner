import { ObjectUtils } from '@/common';
import { caller } from 'tailwindcss';

/**
 * Utility class that can be used to prevent multiple async calls to the same promise. E.g. when calling a lazy fetch
 * endpoint from multiple sources.
 */
export class AsyncDebouncer {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private static pendingRequests = new Map<string, Promise<any>>();

    /**
     * Prevent multiple async calls to the same promise. E.g. when calling a lazy fetch endpoint from multiple
     * sources. If another call to an endpoint is started, the promise of the first call will be returned. This will
     * effectively cause both calls to be fulfilled wih the same promise.
     *
     * @param name name of the call, this must be the same on all calls to this promise
     * @param asyncCall async callback
     */
    public static debounce<T>(name: string, asyncCall: () => Promise<T>): Promise<T> {
        if (this.pendingRequests.has(name)) {
            const pendingPromise = this.pendingRequests.get(name) as Promise<T>;
            return pendingPromise.then((r) => ObjectUtils.deepCopy(r));
        }
        const promise = asyncCall()
            .then((res: T) => {
                this.pendingRequests.delete(name);
                return res;
            })
            .catch((err) => {
                this.pendingRequests.delete(name);
                throw err;
            });
        this.pendingRequests.set(name, promise);
        return promise;
    }
}
