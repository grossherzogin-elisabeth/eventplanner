import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import { mockEvent } from '~/mocks';

describe('EventLocationsCard.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();

    beforeEach(async () => {
        event = mockEvent({
            locations: [
                {
                    name: 'location-0',
                    icon: 'fa-icon-0',
                    order: 0,
                    etd: new Date(`2024-07-10T09:00:00Z`),
                    address: 'location-0-address',
                },
                {
                    name: 'location-1',
                    icon: 'fa-icon-1',
                    order: 1,
                    address: 'location-1-address',
                    addressLink: 'https://www.address-link.com',
                    eta: new Date(`2024-07-10T10:00:00Z`),
                    etd: new Date(`2024-07-10T11:00:00Z`),
                    information: '# information',
                    informationLink: 'https://www.info-link.com',
                },
                {
                    name: 'location-2',
                    icon: 'fa-icon-2',
                    order: 2,
                },
                {
                    name: 'location-3',
                    icon: 'fa-icon-3',
                    order: 3,
                    eta: new Date(`2024-07-10T13:00:00Z`),
                },
            ],
        });
        testee = mount(EventLocationsCard, {
            props: { event },
            global: { stubs: { teleport: true } },
        });
    });

    it('should render all locations', async () => {
        event.locations.forEach((location, index) => {
            const element = testee.find(`[data-test-id="location-${index}"]`);
            expect(element.exists()).toBe(true);
            expect(element.text()).toContain(location.name);
        });
    });

    it('should render event location icons', async () => {
        event.locations.forEach((location) => {
            const icon = testee.find(`i.${location.icon}`);
            expect(icon.exists()).toBe(true);
        });
    });

    it('should not render info icon when no information provided', async () => {
        const location = testee.find('[data-test-id="location-0"]');
        expect(location.find('[data-test-id="context-menu"]').exists()).toBe(false);
    });

    it('should render info icon when location has information set', async () => {
        const location = testee.find('[data-test-id="location-1"]');
        expect(location.find('[data-test-id="context-menu"]').exists()).toBe(true);
    });

    it('should render information when clicking on info icon', async () => {
        const location = testee.find('[data-test-id="location-1"]');
        const btn = location.find('[data-test-id="context-menu"]');
        await btn.trigger('click');
        expect(testee.html()).toContain('<h1>information</h1>');
    });

    it('should render etd date and time', async () => {
        const location = testee.find('[data-test-id="location-0"]');
        const etd = location.find('[data-test-id="etd"]');
        expect(etd.exists()).toBe(true);
        expect(etd.text()).toContain('10.07.');
        expect(etd.text()).toContain('11:00');
    });

    it('should render eta date and time', async () => {
        const location = testee.find('[data-test-id="location-3"]');
        const eta = location.find('[data-test-id="eta"]');
        expect(eta.exists()).toBe(true);
        expect(eta.text()).toContain('10.07.');
        expect(eta.text()).toContain('15:00');
    });

    it('should render neither eta nor etd', async () => {
        const location = testee.find('[data-test-id="location-2"]');
        expect(location.find('[data-test-id="eta"]').exists()).toBe(false);
        expect(location.find('[data-test-id="etd"]').exists()).toBe(false);
    });

    it('should render both eta and etd', async () => {
        const location = testee.find('[data-test-id="location-1"]');
        expect(location.find('[data-test-id="eta"]').exists()).toBe(true);
        expect(location.find('[data-test-id="etd"]').exists()).toBe(true);
    });

    it('should render address with link', async () => {
        const location = testee.find('[data-test-id="location-1"]');
        const address = location.find('[data-test-id="address"]');
        expect(address.exists()).toBe(true);
        expect(address.text()).toContain('location-1-address');
        expect(address.find('a').exists()).toBe(true);
    });
    it('should render address without link', async () => {
        const location = testee.find('[data-test-id="location-0"]');
        const address = location.find('[data-test-id="address"]');
        expect(address.exists()).toBe(true);
        expect(address.text()).toContain('location-0-address');
        expect(address.find('a').exists()).toBe(false);
    });
});
