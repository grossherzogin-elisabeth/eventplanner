import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { type MockInstance, afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useQualificationsAdministrationUseCase } from '@/application';
import { Routes } from '@/ui/views/Routes.ts';
import TabQualifications from '@/ui/views/settings/TabQualifications.vue';
import { mockQualifications, mockRouter } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabQualifications.vue', () => {
    let deleteFunc: MockInstance;
    let testee: VueWrapper;

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        deleteFunc = vi.spyOn(useQualificationsAdministrationUseCase(), 'deleteQualification');
        await router.push({ name: Routes.AppSettings, query: { tab: 'qualifications' } });
        testee = mount(TabQualifications, { global: { plugins: [router], stubs: { teleport: true } } });
    });

    it('should show all qualifications', async () => {
        await loading();
        const qualifications = mockQualifications();
        const table = testee.find('table tbody');
        qualifications.forEach((qualification) => {
            expect(table.text()).toContain(qualification.name);
        });
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(qualifications.length);
    });

    it('should filter qualification by position', async () => {
        await loading();
        const filter = testee.find('[data-test-id="filter-position"]');
        await filter.trigger('click');
        const position = testee.findAll('.dropdown-wrapper li')[1];
        await position.trigger('click');
        await nextTicks(2);
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(1);
        for (const row of rows) {
            expect(row.text()).toContain(position.text());
        }
    });

    it('should filter qualification by expires', async () => {
        await loading();
        const filter = testee.find('[data-test-id="filter-expires"]');
        await filter.trigger('click');
        await nextTicks(2);
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(2);
        for (const row of rows) {
            expect(row.text()).toContain(testee.vm.$t('views.settings.qualifications.status-expires'));
        }
    });

    it('should filter qualification by text', async () => {
        await loading();
        const filter = testee.find('.btn-search input');
        await filter.setValue('captain');
        await nextTicks(2);
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(1);
        for (const row of rows) {
            expect(row.text().toLowerCase()).toContain('captain');
        }
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

    it('should delete qualification on confirm', async () => {
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

    it('should cancel qualification delete', async () => {
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

    it('should open edit dialog for correct qualification', async () => {
        await loading();
        const row = testee.findAll('table tbody tr')[2];
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        const name = row.find('[data-test-id="qualification-name"]').text();
        await testee.find('[data-test-id="context-menu-edit"]').trigger('click');
        const dialog = testee.find('[data-test-id="edit-dialog"]');
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
        expect((dialog.find('[data-test-id="input-name"] input').element as HTMLInputElement).value).toEqual(name);
    });

    async function nextTicks(n: number): Promise<void> {
        for (let i = 0; i < n; i++) {
            await nextTick();
        }
    }

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
