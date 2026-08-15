import type { BaseWrapper } from '@vue/test-utils';

export async function submit(submittable: BaseWrapper<Node>): Promise<void> {
    const submitButtons = submittable.findAll('[data-test-id="button-submit"]');
    if (submitButtons.length !== 1) {
        throw new Error(`Expected exactly one submit button, but found ${submitButtons.length}`);
    }
    await submitButtons[0].trigger('click');
}
