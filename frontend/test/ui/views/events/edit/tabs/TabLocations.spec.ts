import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Event } from '@/domain';
import { Permission } from '@/domain';
import TabLocations from '@/ui/views/events/edit/TabLocations.vue';
import { mockEvent, mockRouter } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabLocations.vue', () => {
    let testee: VueWrapper;
    let event: Event;

    beforeEach(() => {
        setupUserPermissions([Permission.WRITE_EVENTS]);
        event = mockEvent({
            locations: [
                { name: 'LocationA', order: 0, icon: 'fa-icon-a' },
                { name: 'LocationB', order: 1, icon: 'fa-icon-b' },
                { name: 'LocationC', order: 2, icon: 'fa-icon-c' },
                { name: 'LocationD', order: 3, icon: 'fa-icon-d' },
            ],
        });
        testee = mount(TabLocations, {
            props: {
                event,
                // simulate a reactive prop passed as v-model:event
                'onUpdate:event': (e: Event) => testee.setProps({ event: e }),
            },
            global: { plugins: [router] },
        });
    });

    it('Should render all locations', () => {
        const tableRows = testee.findAll('tbody tr');
        expect(event.locations.length).toBeGreaterThan(0);
        expect(tableRows.length).toBe(event.locations.length);
    });

    it('should not render context menu', async () => {
        expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
    });

    describe('users with permission events:write-details', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.WRITE_EVENTS, Permission.WRITE_EVENT_DETAILS]);
        });

        it('should not render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(true);
        });

        it('should render context menu actions', async () => {
            const menu = await openTableContextMenu(testee, 0);
            expect(menu.find('[data-test-id="action-edit"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-move-up"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-move-down"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-delete"]').exists()).toBe(true);
        });

        it('should move location up', async () => {
            let rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('LocationA');
            expect(rows[1].text()).toContain('LocationB');

            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');

            rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('LocationB');
            expect(rows[1].text()).toContain('LocationA');
        });

        it('should emit event on move up', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')?.length).toBe(1);
        });

        it('should not move event above index 0', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');
            const rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('LocationA');
            expect(rows[1].text()).toContain('LocationB');
        });

        it('should move location down', async () => {
            let rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('LocationA');
            expect(rows[1].text()).toContain('LocationB');

            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-down"]');
            await action.trigger('click');

            expect(testee.emitted('update:event')?.length).toBe(1);
            rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('LocationB');
            expect(rows[1].text()).toContain('LocationA');
        });

        it('should emit event on move down', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-down"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')?.length).toBe(1);
        });

        it('should not move event below last index', async () => {
            const menu = await openTableContextMenu(testee, 3);
            const action = menu.find('[data-test-id="action-move-down"]');
            await action.trigger('click');
            const rows = testee.findAll('tbody tr');
            expect(rows[2].text()).toContain('LocationC');
            expect(rows[3].text()).toContain('LocationD');
        });

        it('should remove location', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-delete"]');
            await action.trigger('click');

            expect(testee.emitted('update:event')?.length).toBe(1);
            expect(testee.findAll('tbody tr').length).toBe(3);
            expect(testee.text()).not.toContain('LocationB');
        });

        it('should emit event on remove', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-delete"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')?.length).toBe(1);
        });

        it('should open location edit dialog', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-edit"]');
            await action.trigger('click');
            const dialog = testee.find('[data-test-id="edit-location-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should not emit event on open location edit dialog', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-edit"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')).toBeUndefined();
        });
    });
});
