import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { VMarkdown } from '@/ui/components/common';

describe('VMarkdown', () => {
    it('should render headlines', async () => {
        const testee = mount(VMarkdown, {
            props: {
                value: '# headline 1\n## headline 2',
            },
        });
        const h1 = testee.find('h1');
        expect(h1.exists()).toBe(true);
        expect(h1.text()).toBe('headline 1');

        const h2 = testee.find('h2');
        expect(h2.exists()).toBe(true);
        expect(h2.text()).toBe('headline 2');
    });

    it('should render paragraphs', async () => {
        const testee = mount(VMarkdown, {
            props: {
                value: 'p1\nalso p1\n\nps2',
            },
        });
        const ps = testee.findAll('p');
        expect(ps).toHaveLength(2);
        expect(ps[0].text()).toEqual('p1\nalso p1');
    });

    it('should render lists', async () => {
        const testee = mount(VMarkdown, {
            props: {
                value: '- item 1\n- item 2\n- item 3\n- item 4',
            },
        });
        const ul = testee.find('ul');
        expect(ul.exists()).toBe(true);
        const lis = testee.findAll('ul li');
        expect(lis).toHaveLength(4);
    });

    it('should render links with target _blank', async () => {
        const testee = mount(VMarkdown, {
            props: {
                value: '[label](https:www.example.com)',
            },
        });
        const a = testee.find('a');
        expect(a.exists()).toBe(true);
        expect(a.text()).toEqual('label');
        expect(a.element.target).toBe('_blank');
    });
});
