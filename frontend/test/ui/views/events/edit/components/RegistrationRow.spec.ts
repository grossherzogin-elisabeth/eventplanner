import { nextTick } from 'vue';
import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { ResolvedRegistrationSlot } from '@/domain';
import { RegistrationSlotState } from '@/domain';
import RegistrationRow from '@/ui/views/events/edit/components/RegistrationRow.vue';
import {
    mockPositionCaptain,
    mockPositionDeckhand,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockRegistrationGuest,
    mockSlotCaptain,
    mockUserCaptain,
} from '~/mocks';

const draggableStub = {
    props: ['component'],
    emits: ['dragstart', 'dragend'],
    template:
        '<component :is="component || \'div\'" data-test-id="draggable" @dragstart="$emit(\'dragstart\')" @dragend="$emit(\'dragend\')"><slot /></component>',
};

describe('RegistrationRow.vue', () => {
    let testee: VueWrapper;
    let value: ResolvedRegistrationSlot;

    function mountTestee(): void {
        testee = mount(RegistrationRow, {
            props: { value },
            global: {
                stubs: {
                    VDraggable: draggableStub,
                },
            },
        });
    }

    async function openContextMenu(): Promise<void> {
        await testee.find('button.cursor-pointer').trigger('click');
        await nextTick();
    }

    beforeEach(() => {
        value = {
            name: 'Captain Name',
            state: RegistrationSlotState.ASSIGNED,
            registration: mockRegistrationCaptain({ key: 'reg-captain', userKey: 'user-captain' }),
            slot: mockSlotCaptain({ key: 'slot-captain', assignedRegistrationKey: 'reg-captain' }),
            user: mockUserCaptain({ key: 'user-captain' }),
            position: mockPositionCaptain(),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        mountTestee();
    });

    it('should render guest marker for registration without user', async () => {
        value = {
            ...value,
            name: 'Guest Name',
            registration: mockRegistrationGuest({ key: 'reg-guest', name: 'Guest Name' }),
            slot: undefined,
            user: undefined,
            position: mockPositionDeckhand(),
        };
        mountTestee();

        expect(testee.text()).toContain('Guest Name');
        expect(testee.text()).toContain('(Gastcrew)');
    });

    it('should render deleted user text when user is missing name', async () => {
        value = {
            ...value,
            name: '',
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserCaptain({ key: 'user-deckhand' }),
            position: mockPositionDeckhand(),
        };
        mountTestee();

        expect(testee.text()).toContain('Gelöschter Nutzer');
    });

    it('should emit drag events', async () => {
        const draggable = testee.find('[data-test-id="draggable"]');

        await draggable.trigger('dragstart');
        await draggable.trigger('dragend');

        expect(testee.emitted('dragstart')?.length).toBe(1);
        expect(testee.emitted('dragend')?.length).toBe(1);
    });

    it('should emit addToCrew for waiting-list entry', async () => {
        value = {
            ...value,
            slot: undefined,
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserCaptain({ key: 'user-deckhand' }),
            position: mockPositionDeckhand(),
        };
        mountTestee();

        await openContextMenu();
        const actions = testee.findAll('li.context-menu-item');
        await actions[0].trigger('click');

        expect(testee.emitted('addToCrew')?.length).toBe(1);
    });

    it('should emit removeFromCrew for assigned entry', async () => {
        await openContextMenu();
        await testee.findAll('li.context-menu-item')[0].trigger('click');

        expect(testee.emitted('removeFromCrew')?.length).toBe(1);
    });

    it('should emit editRegistration for assigned entry', async () => {
        await openContextMenu();
        await testee.findAll('li.context-menu-item')[1].trigger('click');

        expect(testee.emitted('editRegistration')?.length).toBe(1);
    });

    it('should emit editSlot for assigned entry', async () => {
        await openContextMenu();
        await testee.findAll('li.context-menu-item')[2].trigger('click');

        expect(testee.emitted('editSlot')?.length).toBe(1);
    });

    it('should emit cancelRegistration for assigned entry', async () => {
        await openContextMenu();
        await testee.findAll('li.context-menu-item')[3].trigger('click');

        expect(testee.emitted('cancelRegistration')?.length).toBe(1);
    });

    it('should emit deleteSlot when slot has no registration', async () => {
        value = {
            ...value,
            name: '',
            registration: undefined,
            user: undefined,
            slot: mockSlotCaptain({ key: 'slot-open', assignedRegistrationKey: undefined }),
            position: mockPositionCaptain(),
        };
        mountTestee();

        await openContextMenu();
        const actions = testee.findAll('li.context-menu-item');
        await actions[3].trigger('click');

        expect(testee.emitted('deleteSlot')?.length).toBe(1);
    });
});
