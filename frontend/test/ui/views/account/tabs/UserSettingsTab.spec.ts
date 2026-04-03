import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserSettings } from '@/domain';
import { Theme } from '@/domain';
import { usePositions } from '@/ui/composables/Positions';
import UserSettingsTab from '@/ui/views/account/tabs/UserSettingsTab.vue';
import { CAPTAIN, mockUserCaptain, mockUserDetails } from '~/mocks';

describe('UserSettingsTab.vue', () => {
    let testee: VueWrapper;
    let userSettings: UserSettings;

    beforeEach(async () => {
        userSettings = {
            preferredPosition: CAPTAIN,
            theme: Theme.System,
            language: 'de',
        };
        testee = mount(UserSettingsTab, {
            props: {
                modelValue: userSettings,
                user: mockUserDetails(mockUserCaptain()),
            },
        });
    });

    it('should render users preferred position', async () => {
        const positions = usePositions();
        await positions.loading;
        expect(testee.text()).toContain(positions.get(userSettings.preferredPosition).name);
    });

    it('should render app design setting', async () => {
        expect(testee.text()).toContain(testee.vm.$t(`generic.theme.${userSettings.theme}`));
    });
});
