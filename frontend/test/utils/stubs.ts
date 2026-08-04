import type { DefineComponent } from 'vue';
import { defineComponent, h } from 'vue';

export function stubs<T = void, P = void>(name: string, spy: (param?: P) => Promise<T>): DefineComponent {
    return defineComponent({
        name: name,
        setup(_, { expose }) {
            expose({
                open: spy,
            });
            return {};
        },
        render() {
            return h('div');
        },
    });
}
