import TabLocations from '@/ui/views/events/edit/TabLocations.vue';
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import {
    given,
    mockAccountRepresentation,
    mockConfigRepresentation,
    mockEvent,
    mockPositionRepresentations,
    mockQualificationRepresentations,
    mockUserRepresentations,
} from '~/mocks';

describe('TabLocations', () => {
    it('should fetch event data initially', () => {
        given.get('/api/v1/config').willReturn(200, mockConfigRepresentation());
        given.get('/api/v1/account').willReturn(200, mockAccountRepresentation());
        given.get('/api/v1/positions').willReturn(200, mockPositionRepresentations());
        given.get('/api/v1/qualifications').willReturn(200, mockQualificationRepresentations());
        given.get('/api/v1/users').willReturn(200, mockUserRepresentations());
        given.get('/api/v1/events/export/templates').willReturn(200, []);

        const testee = mount(TabLocations, {
            props: { event: mockEvent() },
        });
        expect(testee.exists()).toBe(true);
    });
});
