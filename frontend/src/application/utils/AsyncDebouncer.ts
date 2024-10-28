import { deepCopy } from '@/common';

const pendingRequests = new Map<string, Promise<unknown>>();

export async function debounce<T>(name: string, asyncCall: () => Promise<T>): Promise<T> {
    if (pendingRequests.has(name)) {
        const pendingPromise = pendingRequests.get(name) as Promise<T>;
        return pendingPromise.then((r) => deepCopy(r));
    }
    const promise = asyncCall()
        .then((res: T) => {
            pendingRequests.delete(name);
            return res;
        })
        .catch((err) => {
            pendingRequests.delete(name);
            throw err;
        });
    pendingRequests.set(name, promise);
    return promise;
}
