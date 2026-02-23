import type { Ref } from 'vue';
import { onMounted, onUnmounted, ref } from 'vue';

export interface UseViewportSize {
    width: Ref<number>;
    height: Ref<number>;
}

export function useViewportSize(): UseViewportSize {
    const width = ref<number>(0);
    const height = ref<number>(0);

    function update(): void {
        width.value = window.visualViewport?.width || window.innerWidth;
        height.value = window.visualViewport?.height || window.innerHeight;
        document.documentElement.style.setProperty('--viewport-width', `${width.value}px`);
        document.documentElement.style.setProperty('--viewport-height', `${height.value}px`);
    }

    onMounted(() => window.addEventListener('resize', update, { passive: true }));
    onMounted(() => window.addEventListener('scroll', update, { passive: true }));
    onUnmounted(() => window.removeEventListener('resize', update));
    onUnmounted(() => window.removeEventListener('scroll', update));

    update();

    return { width, height };
}
