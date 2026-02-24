import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Event } from '@/domain';
import { Permission } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import TabSlots from '@/ui/views/events/edit/tabs/TabSlots.vue';
import { mockEvent, mockRouter } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabSlots.vue', () => {
    let testee: VueWrapper;
    let event: Event;

    beforeEach(async () => {
        setupUserPermissions([Permission.WRITE_EVENTS]);
        event = mockEvent();
        await usePositions().loading;
        testee = mount(TabSlots, {
            props: {
                event,
                'registrations': [], // TODO
                // simulate a reactive prop passed as v-model:event
                'onUpdate:event': (e: Event) => testee.setProps({ event: e }),
            },
            global: { plugins: [router] },
        });
    });

    it('Should render all slots', () => {
        const rows = testee.findAll('tbody tr');
        expect(event.slots.length).toBeGreaterThan(0);
        expect(rows.length).toBe(event.slots.length);
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
            expect(menu.find('[data-test-id="action-duplicate"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-move-up"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-move-down"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-delete"]').exists()).toBe(true);
        });

        it('should move slot up', async () => {
            let rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('Captain');
            expect(rows[1].text()).toContain('Engineer');

            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');

            rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('Engineer');
            expect(rows[1].text()).toContain('Captain');
        });

        it('should emit event on move up', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')?.length).toBe(1);
        });

        it('should not move slot above index 0', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-up"]');
            await action.trigger('click');
            const rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('Captain');
            expect(rows[1].text()).toContain('Engineer');
        });

        it('should move slot down', async () => {
            let rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('Captain');
            expect(rows[1].text()).toContain('Engineer');

            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-move-down"]');
            await action.trigger('click');

            expect(testee.emitted('update:event')?.length).toBe(1);
            rows = testee.findAll('tbody tr');
            expect(rows[0].text()).toContain('Engineer');
            expect(rows[1].text()).toContain('Captain');
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
            expect(rows[2].text()).toContain('Mate');
            expect(rows[3].text()).toContain('Deckhand');
        });

        it('should remove slot', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-delete"]');
            await action.trigger('click');

            expect(testee.emitted('update:event')?.length).toBe(1);
            expect(testee.findAll('tbody tr').length).toBe(3);
            expect(testee.text()).not.toContain('Engineer');
        });

        it('should emit event on remove', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-delete"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')?.length).toBe(1);
        });

        it('should open dialog for editing on row click', async () => {
            await testee.find('tbody tr').trigger('click');
            const dialog = testee.find('[data-test-id="edit-slot-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should open dialog for editing', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-edit"]');
            await action.trigger('click');
            const dialog = testee.find('[data-test-id="edit-slot-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should not emit event on opening dialog', async () => {
            const menu = await openTableContextMenu(testee, 1);
            const action = menu.find('[data-test-id="action-edit"]');
            await action.trigger('click');
            expect(testee.emitted('update:event')).toBeUndefined();
        });
    });
});
