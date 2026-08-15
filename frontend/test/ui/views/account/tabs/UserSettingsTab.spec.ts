import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserSettings } from '@/domain';
import { Theme } from '@/domain';
import { usePositions } from '@/ui/composables/Positions';
import UserSettingsTab from '@/ui/views/account/tabs/UserSettingsTab.vue';
import { CAPTAIN, MATE, mockUserCaptain, mockUserDetails } from '~/mocks';
import { getLastEmittedModelValue, openCard, selectSelectionListOption, submit } from '~/utils';

describe('UserSettingsTab.vue', () => {
    let testee: VueWrapper;
    let userSettings: UserSettings;
    let positions: ReturnType<typeof usePositions>;

    beforeEach(async () => {
        userSettings = {
            preferredPosition: CAPTAIN,
            theme: Theme.System,
            language: 'de',
        };

        positions = usePositions();
        await positions.loading;

        testee = mount(UserSettingsTab, {
            props: {
                modelValue: userSettings,
                user: mockUserDetails(mockUserCaptain()),
            },
        });
    });

    afterEach(() => testee.unmount());

    function getUpdatedSettings(): UserSettings {
        return getLastEmittedModelValue<UserSettings>(testee);
    }

    describe('theme', () => {
        it('should render selected theme', async () => {
            const themeCard = testee.find('[data-test-id="user-settings-theme-card"]');
            expect(themeCard.exists()).toBe(true);
            expect(themeCard.text()).toContain(testee.vm.$t(`generic.theme.${userSettings.theme}`));
        });

        it('should update theme', async () => {
            const dialog = await openCard(testee, '[data-test-id="user-settings-theme-card"]');
            await selectSelectionListOption(testee, '[data-test-id="user-settings-theme-input"]', 1);

            const updatedSettings = getUpdatedSettings();
            expect(updatedSettings.theme).toEqual(Theme.Dark);
            await dialog.find('[data-test-id="button-cancel"]').trigger('click');
        });
    });

    describe('preferred-position', () => {
        it('should render selected preferred position', async () => {
            const preferredPositionCard = testee.find('[data-test-id="user-settings-preferred-position-card"]');
            expect(preferredPositionCard.exists()).toBe(true);
            expect(preferredPositionCard.text()).toContain('Captain');
        });

        it('should update preferred position', async () => {
            const dialog = await openCard(testee, '[data-test-id="user-settings-preferred-position-card"]');
            await selectSelectionListOption(testee, '[data-test-id="user-settings-preferred-position-input"]', positions.get(MATE).name);
            await submit(dialog);

            const updatedSettings = getUpdatedSettings();
            expect(updatedSettings.preferredPosition).toEqual(MATE);
        });
    });
});
