import { expect } from 'vitest';

export function copyOf<T extends object>(
    original: T
): {
    asymmetricMatch(received: unknown): boolean;
    toString(): string;
    getExpectedType(): string;
} {
    return {
        asymmetricMatch(received: unknown): boolean {
            if (received === original) {
                return false;
            }
            expect(received).toEqual(original);
            return true;
        },
        toString(): string {
            return 'copyOf';
        },
        getExpectedType(): string {
            return 'object';
        },
    };
}
