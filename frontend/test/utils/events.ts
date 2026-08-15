import { expect } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';

export function getLastEmittedModelValue<T>(testee: VueWrapper<unknown>, eventName = 'update:modelValue'): T {
    const emitted = testee.emitted(eventName);
    expect(emitted).toBeTruthy();
    expect(emitted).not.toHaveLength(0);
    return emitted?.[emitted.length - 1][0] as T;
}
