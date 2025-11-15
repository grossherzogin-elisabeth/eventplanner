import { vi } from 'vitest';

export interface MockedRequest<T, B> {
    method: string;
    url: string;
    body: B;
    status: number;
    response: T;
    once: boolean;
}

const realFetch = global.fetch;
const mocks: MockedRequest<unknown, unknown>[] = [];

global.fetch = vi.fn().mockImplementation((url: string, req?: RequestInit) => {
    const method = req?.method ?? 'GET';
    const index = mocks.findIndex((m) => m.method === method && m.url === url);
    if (index >= 0) {
        const mock = mocks[index];
        if (mock.once) {
            console.log(`Mocking response for request ${method} ${url} (last time)`);
            mocks.splice(index, 1);
        } else {
            console.log(`Mocking response for request ${method} ${url}`);
        }
        return Promise.resolve({
            status: mock.status,
            ok: mock.status >= 200 && mock.status < 400,
            json: vi.fn().mockResolvedValueOnce(mock.response),
            text: vi.fn().mockResolvedValueOnce(String(mock.response)),
            clone: vi.fn().mockReturnThis(),
        });
    }
    console.log(`No mock registered for request ${method} ${url}, performing real fetch`);
    return realFetch(url, req);
});

export function clearMockedRequests(): void {
    mocks.splice(0, mocks.length);
}

export class MockRequestBuilder<B> {
    private readonly method: string;
    private readonly url: string;
    private readonly body?: B;

    constructor(method: string, url: string, body?: B) {
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public willReturn<T>(status: number, response?: T, once?: boolean): void {
        mocks.push({
            method: this.method,
            url: this.url,
            body: this.body,
            status: status,
            response: response,
            once: once === true,
        });
    }

    public static get(url: string): MockRequestBuilder<undefined> {
        return new MockRequestBuilder('GET', url);
    }

    public static post<B>(url: string, body?: B): MockRequestBuilder<B> {
        return new MockRequestBuilder('POST', url, body);
    }

    public static put<B>(url: string, body?: B): MockRequestBuilder<B> {
        return new MockRequestBuilder('PUT', url, body);
    }

    public static patch<B>(url: string, body?: B): MockRequestBuilder<B> {
        return new MockRequestBuilder('PATCH', url, body);
    }

    public static delete(url: string): MockRequestBuilder<undefined> {
        return new MockRequestBuilder('DELETE', url);
    }
}

export const given = MockRequestBuilder;
