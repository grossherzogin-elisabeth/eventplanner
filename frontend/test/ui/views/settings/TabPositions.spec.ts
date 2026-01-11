import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { type MockInstance, afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { usePositionAdministrationUseCase } from '@/application';
import { Routes } from '@/ui/views/Routes.ts';
import TabPositions from '@/ui/views/settings/TabPositions.vue';
import { mockPositions } from '~/mocks';
import { mockRouter } from '~/mocks/router/mockRouter.ts';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabPositions.vue', () => {
    let deleteFunc: MockInstance;
    let testee: VueWrapper;

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        deleteFunc = vi.spyOn(usePositionAdministrationUseCase(), 'deletePosition');
        await router.push({ name: Routes.AppSettings, query: { tab: 'positions' } });
        testee = mount(TabPositions, { global: { plugins: [router], stubs: { teleport: true } } });
    });

    it('should show all positions', async () => {
        await loading();
        const positions = mockPositions();
        const table = testee.find('table tbody');
        for (const position of positions) {
            expect(table.text()).toContain(position.name);
        }
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(positions.length);
    });

    it('should open delete confirm dialog', async () => {
        await loading();
        const row = testee.findAll('table tbody tr')[2];
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-delete"]').trigger('click');
        const dialog = testee.find('[data-test-id="delete-confirm-dialog"]');
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
    });

    it('should delete position on confirm', async () => {
        await loading();
        const row = testee.findAll('table tbody tr')[2];
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-delete"]').trigger('click');
        const confirmButton = testee.find('[data-test-id="button-confirm"]');
        await confirmButton.trigger('click');
        vi.runAllTimers();
        await nextTick();
        await nextTick();
        expect(deleteFunc).toHaveBeenCalled();
    });

    it('should cancel position delete', async () => {
        await loading();
        const row = testee.findAll('table tbody tr')[2];
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-delete"]').trigger('click');
        const confirmButton = testee.find('[data-test-id="button-cancel"]');
        await confirmButton.trigger('click');
        vi.runAllTimers();
        await nextTick();
        await nextTick();
        expect(deleteFunc).not.toHaveBeenCalled();
    });

    it('should open edit dialog for correct position', async () => {
        await loading();
        const row = testee.findAll('table tbody tr')[2];
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        const name = row.find('[data-test-id="position-name"]').text();
        await testee.find('[data-test-id="context-menu-edit"]').trigger('click');
        const dialog = testee.find('[data-test-id="edit-dialog"]');
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
        expect((dialog.find('[data-test-id="input-name"] input').element as HTMLInputElement).value).toEqual(name);
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
