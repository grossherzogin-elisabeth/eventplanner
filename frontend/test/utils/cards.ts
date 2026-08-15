import { nextTick } from 'vue';
import { expect } from 'vitest';
import type { BaseWrapper, DOMWrapper } from '@vue/test-utils';

export async function openCard(testee: BaseWrapper<Node>, cardSelector: string): Promise<DOMWrapper<Element>> {
    const card = testee.find(cardSelector);
    if (card.exists()) {
        await card.trigger('click');
    } else {
        const element = document.querySelector(cardSelector);
        expect(element).toBeTruthy();
        element?.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
        await nextTick();
    }

    const dialog = testee.find('[data-test-id="dialog"]');
    expect(dialog.exists()).toBe(true);
    return dialog;
}
