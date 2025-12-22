import { expect } from 'vitest';
import { DOMWrapper } from '@vue/test-utils';

export function find<T extends Element>(selector: string): DOMWrapper<T> {
    const elements = document.querySelectorAll(selector);
    expect(elements).toHaveLength(1);
    return new DOMWrapper(elements[0] as T);
}

export function findAll<T extends Element>(selector: string): DOMWrapper<T>[] {
    return [...document.querySelectorAll(selector)].map((element) => new DOMWrapper(element as T));
}
