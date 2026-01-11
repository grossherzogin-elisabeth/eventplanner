import { nextTick } from 'vue';
import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService } from '@/application';
import { wait } from '@/common';
import type { Position } from '@/domain';
import { Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import PositionDetailsDlg from '@/ui/views/settings/components/PositionDetailsDlg.vue';
import { mockPositionMate, mockSignedInUser } from '~/mocks';

describe('PositionDetailsDlg.vue', () => {
    let testee: VueWrapper;
    let result: Position | undefined = undefined;
    let closed: boolean = false;

    beforeEach(async () => {
        result = undefined;
        closed = false;
        useAuthService().setSignedInUser(mockSignedInUser({ permissions: [Permission.READ_POSITIONS, Permission.WRITE_POSITIONS] }));
        testee = mount(PositionDetailsDlg, { global: { stubs: { teleport: true } } });
    });

    describe('create mode', () => {
        beforeEach(async () => {
            await open(undefined);
        });

        it('should open dialog and show correct title', async () => {
            const dialog = testee.find('[data-test-id="position-details-dialog"]');
            expect(dialog.isVisible()).toBe(true);
            expect(testee.find('h1').text()).toEqual(testee.vm.$t('views.settings.positions.add-new'));
        });

        it('should close dialog on cancel and return undefined', async () => {
            await cancel();
            expect(result).toBe(undefined);
        });

        it('should enable key input', async () => {
            const input = testee.find('[data-test-id="input-key"] input');
            expect((input.element as HTMLInputElement).disabled).toBe(false);
        });

        it('should not show validation errors directly when creating', async () => {
            const input = testee.find('[data-test-id="input-name"] input');
            expect(input.classes()).not.toContain('invalid');
        });
    });

    describe('edit mode', () => {
        let position: Position = mockPositionMate();

        beforeEach(async () => {
            position = mockPositionMate();
            await open(position);
        });

        it('should open dialog and show correct title', async () => {
            const dialog = testee.find('[data-test-id="position-details-dialog"]');
            expect(dialog.isVisible()).toBe(true);
            expect(testee.find('h1').text()).toEqual(testee.vm.$t('views.settings.positions.edit'));
        });

        it('should close dialog on cancel and return undefined', async () => {
            await cancel();
            expect(result).toBe(undefined);
        });

        it('should return edited position on submit', async () => {
            const input = testee.find('[data-test-id="input-name"] input');
            await input.setValue('updated');
            await submit();
            expect(result).toEqual(mockPositionMate({ name: 'updated' }));
        });

        it('should enable key input', async () => {
            const input = testee.find('[data-test-id="input-key"] input');
            expect((input.element as HTMLInputElement).disabled).toBe(true);
        });

        it('should render key', async () => {
            const input = getInputElement('[data-test-id="input-key"] input');
            expect(input.value).toEqual(position.key);
        });

        it('should render name', async () => {
            const input = getInputElement('[data-test-id="input-name"] input');
            expect(input.value).toEqual(position.name);
        });

        it('should render color', async () => {
            const input = getInputElement('[data-test-id="input-color"] input');
            expect(input.value).toEqual(position.color);
        });

        it('should render prio', async () => {
            const input = getInputElement('[data-test-id="input-prio"] input');
            expect(input.value).toEqual(String(position.prio));
        });

        it('should render imoListRank', async () => {
            const input = getInputElement('[data-test-id="input-imo-list-rank"] input');
            expect(input.value).toEqual(position.imoListRank);
        });

        it('should show validation errors directly', async () => {
            let input = testee.find('[data-test-id="input-name"] input');
            await input.setValue('');
            input = testee.find('[data-test-id="input-name"] input');
            expect(input.classes()).toContain('invalid');
            expect(closed).toBe(false);
        });
    });

    function getInputElement(selector: string): HTMLInputElement {
        const input = testee.find(selector);
        return input.element as HTMLInputElement;
    }

    async function cancel(): Promise<void> {
        await testee.find('[data-test-id="button-cancel"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function submit(): Promise<void> {
        await testee.find('[data-test-id="button-submit"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function open(param: Position | undefined): Promise<void> {
        const dialog = testee.getCurrentComponent().exposed as Dialog<Position | undefined, Position | undefined>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(param).then((r) => {
            closed = true;
            result = r;
        });
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }
});
