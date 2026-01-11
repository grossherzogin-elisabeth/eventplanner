import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { type MockInstance, afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { usePositionAdministrationUseCase } from '@/application';
import { Routes } from '@/ui/views/Routes';
import TabPositions from '@/ui/views/settings/TabPositions.vue';
import { mockPositions, mockRouter } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabPositions.vue', () => {
    let deleteFunc: MockInstance;
    let updateFunc: MockInstance;
    let testee: VueWrapper;

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        deleteFunc = vi.spyOn(usePositionAdministrationUseCase(), 'deletePosition');
        updateFunc = vi.spyOn(usePositionAdministrationUseCase(), 'updatePosition');
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
        const dialog = await openDeleteDialog(getRow(2));
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
    });

    it('should delete on confirm', async () => {
        await loading();
        const dialog = await openDeleteDialog(getRow(2));
        await dialog.find('[data-test-id="button-confirm"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
        expect(deleteFunc).toHaveBeenCalled();
    });

    it('should cancel delete', async () => {
        await loading();
        const dialog = await openDeleteDialog(getRow(2));
        await dialog.find('[data-test-id="button-cancel"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
        expect(deleteFunc).not.toHaveBeenCalled();
    });

    it('should open edit dialog for correct position', async () => {
        await loading();
        const row = getRow(2);
        const dialog = await openEditDialog(row);
        const name = row.find('[data-test-id="position-name"]').text();
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
        expect(getInputElement('[data-test-id="input-name"] input').value).toEqual(name);
    });

    it('should cancel edit', async () => {
        await loading();
        const dialog = await openEditDialog(getRow(2));
        await dialog.find('[data-test-id="button-cancel"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
        expect(updateFunc).not.toHaveBeenCalled();
    });

    it('should save changes', async () => {
        await loading();
        const dialog = await openEditDialog(getRow(2));
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
        expect(updateFunc).toHaveBeenCalled();
    });

    function getInputElement(selector: string): HTMLInputElement {
        return testee.find(selector).element as HTMLInputElement;
    }

    function getRow(rowIndex: number): DOMWrapper<Element> {
        return testee.findAll('table tbody tr')[rowIndex];
    }

    async function openDeleteDialog(row: DOMWrapper<Element>): Promise<DOMWrapper<Element>> {
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-delete"]').trigger('click');
        return testee.find('[data-test-id="delete-confirm-dialog"]');
    }

    async function openEditDialog(row: DOMWrapper<Element>): Promise<DOMWrapper<Element>> {
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-edit"]').trigger('click');
        return testee.find('[data-test-id="edit-dialog"]');
    }

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
