import { onMounted, onUnmounted, ref } from 'vue';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useViewportSize() {
    const width = ref<number>(0);
    const height = ref<number>(0);

    function update(): void {
        width.value = window.visualViewport?.width || window.innerWidth;
        height.value = window.visualViewport?.height || window.innerHeight;
        document.documentElement.style.setProperty('--viewport-width', `${width.value}px`);
        document.documentElement.style.setProperty('--viewport-height', `${height.value}px`);
    }

    onMounted(() => window.addEventListener('resize', update));
    onUnmounted(() => window.removeEventListener('resize', update));

    update();

    return { width, height };
}
