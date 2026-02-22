import { nextTick } from 'vue';
import { expect } from 'vitest';
import type { BaseWrapper } from '@vue/test-utils';
import type { DOMWrapper } from '@vue/test-utils';

export function getTabs(testee: BaseWrapper<Node>): DOMWrapper<Element> {
    return testee.find('[data-test-id="tabbar"]');
}

export async function openPageContextMenu(testee: BaseWrapper<Node>): Promise<DOMWrapper<Element>> {
    const triggers = testee.findAll('[data-test-id="menu-trigger"]').filter((trigger) => trigger.isVisible());
    await triggers[0].find('button').trigger('click');
    await nextTick();
    return testee.find('[data-test-id="context-menu"]');
}

export async function openTableContextMenu(testee: BaseWrapper<Node>, row: number | BaseWrapper<Node>): Promise<DOMWrapper<Element>> {
    const trigger =
        typeof row === 'number'
            ? testee.findAll('[data-test-id="table-context-menu-trigger"]')[row]
            : row.find('[data-test-id="table-context-menu-trigger"]');
    expect(trigger.exists()).toBe(true);
    await trigger.find('button').trigger('click');
    await nextTick();
    return testee.find('[data-test-id="table-context-menu"]');
}

export async function awaitPageContentLoaded(testee: BaseWrapper<Node>, expectedTitle?: string): Promise<void> {
    const titleElement = testee.find('[data-test-id="title"]');
    if (expectedTitle) {
        await expect.poll(() => titleElement.text()).includes(expectedTitle);
    } else {
        await expect.poll(() => titleElement.classes()).not.toContain('loading');
    }
    await nextTick();
}
