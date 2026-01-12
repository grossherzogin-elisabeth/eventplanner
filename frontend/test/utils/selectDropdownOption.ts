import { nextTick } from 'vue';
import { expect } from 'vitest';
import type { BaseWrapper, DOMWrapper } from '@vue/test-utils';

export async function selectDropdownOption(parent: BaseWrapper<Node>, option: string | number | RegExp): Promise<void> {
    const options = parent.findAll('.input-dropdown li');
    let li: DOMWrapper<Element> | undefined;
    if (typeof option === 'string') {
        li = options.find((it) => it.text() == option) ?? undefined;
    } else if (typeof option === 'number') {
        li = options[option];
    } else {
        li = options.find((it) => option.test(it.text())) ?? undefined;
    }
    expect(li).toBeDefined();
    expect(li?.exists()).toBe(true);
    await li?.trigger('click');
    await nextTick();
}
