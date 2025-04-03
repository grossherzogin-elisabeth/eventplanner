<template>
    <g class="fill-current stroke-current">
        <line
            :x1="props.pointer.x1"
            :y1="props.pointer.y1"
            :x2="props.pointer.x2"
            :y2="props.pointer.y2"
            :stroke-dasharray="props.dash"
            stroke-width="1px"
        />
        <path
            v-if="props.arrowStart || props.arrowEnd"
            :d="`M ${arrowTipA.x} ${arrowTipA.y} L ${arrowTipB.x} ${arrowTipB.y} L ${arrowTipC.x} ${arrowTipC.y}`"
        ></path>
        <!--        <text :x="props.pointer.x2" :y="props.pointer.y2">{{ angle }}°</text>-->
    </g>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import type { Point, Pointer } from '@/ui/views/wind-calculator/Pointer.ts';
import { calculatePointerDirection } from '@/ui/views/wind-calculator/Pointer.ts';
import { calculatePointer } from '@/ui/views/wind-calculator/Pointer.ts';

interface Props {
    pointer: Pointer;
    dash?: number;
    arrowStart?: boolean;
    arrowEnd?: boolean;
}
const props = defineProps<Props>();

const arrowHeadLength = 10;

const arrowHeadAngle = computed<number>(() => {
    if (props.arrowStart) {
        return 20;
    }
    return 160;
});

const angle = computed<number>(() => calculatePointerDirection(props.pointer));

const arrowTipA = computed<Point>(() => {
    if (props.arrowStart) {
        return { x: props.pointer.x1, y: props.pointer.y1 };
    }
    return { x: props.pointer.x2, y: props.pointer.y2 };
});

const arrowTipB = computed<Point>(() => {
    const p = calculatePointer(arrowTipA.value, angle.value - arrowHeadAngle.value, arrowHeadLength);
    return { x: p.x2, y: p.y2 };
});

const arrowTipC = computed<Point>(() => {
    const p = calculatePointer(arrowTipA.value, angle.value + arrowHeadAngle.value, arrowHeadLength);
    return { x: p.x2, y: p.y2 };
});
</script>
