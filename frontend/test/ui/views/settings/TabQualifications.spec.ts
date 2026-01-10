import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthUseCase } from '@/application';
import { setupRouter } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import TabQualifications from '@/ui/views/settings/TabQualifications.vue';
import { mockQualifications } from '~/mocks';

describe('TabQualifications.vue', () => {
    let router: Router;
    let testee: VueWrapper;

    beforeAll(() => {
        router = setupRouter(useAuthUseCase());
    });

    beforeEach(async () => {
        testee = mount(TabQualifications, { global: { plugins: [router], stubs: { teleport: true } } });
        await router.push({ name: Routes.AppSettings });
    });

    it('should show all qualifications', () => {
        const qualifications = mockQualifications();
        const table = testee.find('table tbody');
        qualifications.forEach((qualification) => {
            expect(table.text()).toContain(qualification.name);
        });
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(qualifications.length);
    });

    it('should filter qualification by position', async () => {
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
        const filter = testee.find('.btn-search input');
        await filter.setValue('captain');
        await nextTicks(2);
        const rows = testee.findAll('table tbody tr');
        expect(rows).toHaveLength(1);
        for (const row of rows) {
            expect(row.text().toLowerCase()).toContain('captain');
        }
    });

    async function nextTicks(n: number): Promise<void> {
        for (let i = 0; i < n; i++) {
            await nextTick();
        }
    }
});
