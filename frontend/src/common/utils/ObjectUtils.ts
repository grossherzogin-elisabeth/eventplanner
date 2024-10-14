import { ArrayUtils } from '@/common';

/**
 * A collection of utility functions for objects
 */
export class ObjectUtils {
    /**
     * Create a real deep copy of the object
     * @param value
     */
    public static deepCopy<T>(value: T): T {
        // null / undefined
        if (value === null || value === undefined) {
            return value;
        }
        // recursively clone array
        if (Array.isArray(value)) {
            return value.map((item) => ObjectUtils.deepCopy(item)) as unknown as T;
        }
        // create wrapper for function
        if (typeof value === 'function') {
            // create a wrapper for the function so the original function cannot be overwritten by assigning the
            // function in the copy
            return ((...args: never[]) => value(...args)) as unknown as T;
        }
        // clone date
        if (value instanceof Date) {
            return new Date(value.getTime()) as unknown as T;
        }
        // recursively clone object
        if (typeof value === 'object') {
            // should also work for regex
            // if (value instanceof RegExp) {
            //     console.log('value is a regex');
            // }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const copy: any = {};
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            Object.entries(value).forEach((kv: [string, any]) => {
                copy[kv[0]] = ObjectUtils.deepCopy(kv[1]);
            });
            return copy;
        }
        // primitives (string, number, boolean, ...) that are call by value
        return value;
    }

    /**
     * Create a simple hash of the provided object. Note: this function is not cryptographically safe, so don't use
     * this to hash passwords or other secrets!
     * @param object the object to hash
     */
    public static hash<T>(object: T): string {
        const str = JSON.stringify(object);
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            // eslint-disable-next-line no-bitwise
            hash = (hash << 5) - hash + char;
            // eslint-disable-next-line no-bitwise
            hash &= hash; // Convert to 32bit integer
        }
        if (hash < 0) {
            return `1${(hash * -1).toString(16)}`;
        }
        return hash.toString(16);
    }

    /**
     * @param item any object type
     * @param field fieldname or path (e.g. 'subobject.subsubobject.field'
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public static extractValue(item: any, field: string): any {
        const parts: string[] = field.split('.');
        const next: string = parts.shift() || '';
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const value: any = item[next];
        if (Array.isArray(value)) {
            if (parts.length === 0) {
                return value;
            }
            console.warn(
                `Nested array indices are currently not supported in table sorting! Tried to extract ${next}.`
            );
            return undefined;
        }
        if (typeof value === 'object') {
            if (parts.length === 0) {
                if ((value as Date).getTime) {
                    return value;
                }
                return JSON.stringify(value);
            }
            return ObjectUtils.extractValue(value, parts.join('.'));
        }

        if (parts.length !== 0) {
            console.warn(`Cannot extract a sub object from a non object value! Tried to extract ${next}.`);
            return undefined;
        }
        return value;
    }

    public static diff<T extends object>(oldValue: T, newValue: T): Partial<T> {
        const diff: Partial<T> = {};
        Object.keys(oldValue)
            .concat(Object.keys(newValue))
            .filter(ArrayUtils.filterDuplicates)
            .filter((key) => {
                const a = (oldValue as Record<string, unknown>)[key];
                const b = (newValue as Record<string, unknown>)[key];
                return !ObjectUtils.deepEquals(a, b);
            })
            .forEach((key) => ((diff as Record<string, unknown>)[key] = (newValue as Record<string, unknown>)[key]));
        return diff;
    }

    private static deepEquals(a: unknown, b: unknown): boolean {
        if (a === undefined || b === undefined) {
            return a === undefined && b === undefined;
        }
        if (a === null || b === null) {
            return a === null && b === null;
        }
        if (Array.isArray(a) || Array.isArray(b)) {
            if (!Array.isArray(a) || !Array.isArray(b)) return false;
            if (a.length !== b.length) return false;
            for (let i = 0; i < a.length; i++) {
                if (!ObjectUtils.deepEquals(a[i], b[i])) return false;
            }
            return true;
        }
        if (a instanceof Date || b instanceof Date) {
            if (!(a instanceof Date) || !(b instanceof Date)) return false;
            return a.getTime() === b.getTime();
        }
        if (typeof a === 'object' || typeof b === 'object') {
            if (typeof a !== 'object' || typeof b !== 'object') return false;
            const keys = Object.keys(a).concat(Object.keys(b)).filter(ArrayUtils.filterDuplicates);
            for (const key of keys) {
                const a2 = (a as Record<string, unknown>)[key];
                const b2 = (b as Record<string, unknown>)[key];
                if (!ObjectUtils.deepEquals(a2, b2)) return false;
            }
            return true;
        }
        return a === b;
    }
}
