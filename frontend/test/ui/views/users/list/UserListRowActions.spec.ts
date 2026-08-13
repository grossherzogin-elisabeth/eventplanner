import { afterEach, describe, expect, it } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { User } from '@/domain';
import UserListRowActions from '@/ui/views/users/list/UserListRowActions.vue';
import { mockUserCaptain, mockUserEngineer } from '~/mocks';

describe('UserListRowActions.vue', () => {
    let testee: VueWrapper;

    afterEach(() => testee?.unmount());

    it('should render no actions when no users are provided', () => {
        testee = mount(UserListRowActions);
        expect(action('action-contact').exists()).toBe(false);
        expect(action('action-impersonate').exists()).toBe(false);
        expect(action('action-create-registration').exists()).toBe(false);
        expect(action('action-edit').exists()).toBe(false);
        expect(action('action-delete').exists()).toBe(false);
    });

    it('should render only contact action for multiple users', () => {
        const users = [mockUserCaptain(), mockUserEngineer()];
        testee = mountTestee(users);

        expect(action('action-contact').exists()).toBe(true);
        expect(action('action-impersonate').exists()).toBe(false);
        expect(action('action-create-registration').exists()).toBe(false);
        expect(action('action-edit').exists()).toBe(false);
        expect(action('action-delete').exists()).toBe(false);
    });

    it('should emit all single-user actions', async () => {
        const user = mockUserCaptain();
        testee = mountTestee([user]);

        await action('action-contact').trigger('click');
        await action('action-impersonate').trigger('click');
        await action('action-create-registration').trigger('click');
        await action('action-edit').trigger('click');
        await action('action-delete').trigger('click');

        expect(testee.emitted('contact')?.[0]).toEqual([[user]]);
        expect(testee.emitted('impersonate')?.[0]).toEqual([user]);
        expect(testee.emitted('create-registration')?.[0]).toEqual([user]);
        expect(testee.emitted('delete')?.[0]).toEqual([user]);
        expect(testee.emitted('edit')?.[0]?.[0]).toMatchObject({ user });
    });

    function mountTestee(users: User[]): VueWrapper {
        return mount(UserListRowActions, { props: { users } });
    }

    function action(testId: string): DOMWrapper<Element> {
        return testee.find(`[data-test-id="${testId}"]`);
    }
});
