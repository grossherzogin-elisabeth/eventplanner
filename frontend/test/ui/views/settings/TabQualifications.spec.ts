import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { type MockInstance, afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService, useQualificationsAdministrationUseCase } from '@/application';
import { Permission } from '@/domain';
import { VConfirmationDialog } from '@/ui/components/common';
import { Routes } from '@/ui/views/Routes';
import TabQualifications from '@/ui/views/settings/TabQualifications.vue';
import QualificationDetailsDlg from '@/ui/views/settings/components/QualificationDetailsDlg.vue';
import { mockQualifications, mockRouter, mockSignedInUser } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabQualifications.vue', () => {
    let deleteFunc: MockInstance;
    let updateFunc: MockInstance;
    let createFunc: MockInstance;
    let testee: VueWrapper;

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        deleteFunc = vi.spyOn(useQualificationsAdministrationUseCase(), 'deleteQualification');
        updateFunc = vi.spyOn(useQualificationsAdministrationUseCase(), 'updateQualification');
        createFunc = vi.spyOn(useQualificationsAdministrationUseCase(), 'createQualification');
        await router.push({ name: Routes.AppSettings, query: { tab: 'qualifications' } });
    });

    describe('users with permission qualifications:read', () => {
        beforeEach(async () => {
            useAuthService().setSignedInUser(mockSignedInUser({ permissions: [Permission.READ_QUALIFICATIONS] }));
            testee = mount(TabQualifications, { global: { plugins: [router] } });
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

        it('should not render context menu', async () => {
            await loading();
            expect(getRow(2).find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
        });

        it('should not render create button', async () => {
            await loading();
            expect(testee.find('[data-test-id="button-create"]').exists()).toBe(false);
        });
    });

    describe('users with permission qualifications:write', () => {
        beforeEach(async () => {
            useAuthService().setSignedInUser(
                mockSignedInUser({ permissions: [Permission.READ_QUALIFICATIONS, Permission.WRITE_QUALIFICATIONS] })
            );
            testee = mount(TabQualifications, { global: { plugins: [router] } });
        });

        it('should open delete confirm dialog', async () => {
            await loading();
            const dialog = await openDeleteDialog(getRow(2));
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should delete qualification on confirm', async () => {
            await loading();
            const dialog = await openDeleteDialog(getRow(2));
            await dialog.find('[data-test-id="button-confirm"]').trigger('click');
            vi.runAllTimers();
            await nextTick();
            expect(deleteFunc).toHaveBeenCalled();
        });

        it('should cancel qualification delete', async () => {
            await loading();
            const dialog = await openDeleteDialog(getRow(2));
            await dialog.find('[data-test-id="button-cancel"]').trigger('click');
            vi.runAllTimers();
            await nextTick();
            expect(deleteFunc).not.toHaveBeenCalled();
        });

        it('should open create dialog', async () => {
            await loading();
            const dialog = await openCreateDialog();
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
            expect(getInputElement('[data-test-id="input-name"] input').value).toEqual('');
        });

        it('should create new qualification', async () => {
            await loading();
            await openCreateDialog();
            await testee.find('[data-test-id="input-key"] input').setValue('key');
            await testee.find('[data-test-id="input-name"] input').setValue('name');
            await testee.find('[data-test-id="input-icon"] input').setValue('fa-id-card');
            await testee.find('[data-test-id="input-description"] textarea').setValue('description');
            await testee.find('[data-test-id="button-submit"]').trigger('click');
            vi.runAllTimers();
            await nextTick();
            expect(createFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    key: 'key',
                    name: 'name',
                    description: 'description',
                    icon: 'fa-id-card',
                })
            );
        });

        it('should open edit dialog for correct qualification', async () => {
            await loading();
            const row = getRow(2);
            const name = row.find('[data-test-id="qualification-name"]').text();
            const dialog = await openEditDialog(row);
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
            await testee.find('[data-test-id="input-name"] input').setValue('changed');
            await dialog.find('[data-test-id="button-submit"]').trigger('click');
            vi.runAllTimers();
            await nextTick();
            expect(updateFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    name: 'changed',
                })
            );
        });
    });

    async function nextTicks(n: number): Promise<void> {
        for (let i = 0; i < n; i++) {
            await nextTick();
        }
    }

    function getInputElement(selector: string): HTMLInputElement {
        return testee.find(selector).element as HTMLInputElement;
    }

    function getRow(rowIndex: number): DOMWrapper<Element> {
        return testee.findAll('table tbody tr')[rowIndex];
    }

    async function openCreateDialog(): Promise<VueWrapper> {
        await testee.find('[data-test-id="button-create"]').trigger('click');
        return testee.findComponent(QualificationDetailsDlg);
    }

    async function openDeleteDialog(row: DOMWrapper<Element>): Promise<VueWrapper> {
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-delete"]').trigger('click');
        return testee.findComponent(VConfirmationDialog);
    }

    async function openEditDialog(row: DOMWrapper<Element>): Promise<VueWrapper> {
        await row.find('[data-test-id="table-context-menu-trigger"]').trigger('click');
        await testee.find('[data-test-id="context-menu-edit"]').trigger('click');
        return testee.findComponent(QualificationDetailsDlg);
    }

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
