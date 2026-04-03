import type { RouteLocationNormalizedLoadedGeneric } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { RouterLinkStub } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import SiteNavigation from '@/ui/components/partials/SiteNavigation.vue';
import { mockRouter } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRoute: (): Partial<RouteLocationNormalizedLoadedGeneric> => router.currentRoute.value,
}));

describe('SiteNavigation', () => {
    let testee: VueWrapper;

    beforeEach(() => {
        document.body.innerHTML = `
            <div id="app">
                <div id="container">
                    <h2 id="section1" class="site-link">Section 1 Title</h2>
                    <p>Some content for section 1.</p>
                    <div style="height: 500px;"></div> <!-- Spacer to make scrolling relevant -->
                    <h2 id="section2" class="site-link">Section 2 Title</h2>
                    <p>More content for section 2.</p>
                    <div style="height: 500px;"></div>
                    <!-- Link with no text content, should be ignored -->
                    <h2 id="section3-no-text" class="site-link"></h2>
                </div>
            </div>`;
        testee = mount(SiteNavigation, { global: { plugins: [router] } });
    });

    it('should detect links correctly', () => {
        expect(testee.findAll('a')).toHaveLength(2);
        expect(testee.text()).toContain('Section 1 Title');
        expect(testee.text()).toContain('Section 2 Title');
    });

    it('should create hash links', () => {
        const link = testee.findComponent(RouterLinkStub);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        expect((link.props().to as any).hash).toEqual('#section1');
    });

    it('should scroll to link target on click', () => {
        const linkTarget = document.getElementById('section2');
        expect(linkTarget).not.toBeNull();
        if (linkTarget) {
            const scrollIntoViewFunction = vi.spyOn(linkTarget, 'scrollIntoView');
            testee.findAll('a')[1].trigger('click');
            expect(scrollIntoViewFunction).toHaveBeenCalled();
        }
    });
});
