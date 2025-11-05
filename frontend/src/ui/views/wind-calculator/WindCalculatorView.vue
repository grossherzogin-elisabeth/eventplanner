<template>
    <div class="xl:overflow-auto">
        <div class="xs:px-8 px-4 py-4 md:px-16 lg:pt-10 xl:px-20">
            <div class="flex flex-col items-stretch gap-8 xl:flex-row xl:gap-16">
                <div class="w-full md:max-w-sm">
                    <section>
                        <h2 class="text-secondary mb-4 font-bold">Schiffsdaten</h2>
                        <div class="mb-4">
                            <VInputSlider v-model="hdg" label="Heading (HDG)" :min="0" :max="360">
                                <template #after><span class="text-onsurface-variant/50 pr-4">deg</span></template>
                            </VInputSlider>
                        </div>
                        <div class="mb-4">
                            <VInputSlider v-model="cog" label="Course over ground(COG)" :min="0" :max="360">
                                <template #after><span class="text-onsurface-variant/50 pr-4">deg</span></template>
                            </VInputSlider>
                        </div>
                        <div class="mb-8">
                            <VInputNumber v-modael="sog" label="Speed over ground (SOG)">
                                <template #after><span class="text-onsurface-variant/50 pr-4">kn</span></template>
                            </VInputNumber>
                        </div>
                    </section>

                    <section>
                        <h2 class="text-secondary mb-4 font-bold">Scheinbarer Wind</h2>
                        <div class="mb-4">
                            <VInputNumber
                                label="Scheinbare Windgeschwindigkeit"
                                :model-value="relativeWindSpeed"
                                @update:model-value="recalculateFromRelativeWind($event, relativeWindDirection)"
                            >
                                <template #after><span class="text-onsurface-variant/50 pr-4">kn</span></template>
                            </VInputNumber>
                        </div>
                        <div class="mb-8">
                            <VInputSlider
                                label="Scheinbare Windrichtung"
                                :model-value="relativeWindDirection"
                                :min="-180"
                                :max="180"
                                @update:model-value="recalculateFromRelativeWind(relativeWindSpeed, $event)"
                            >
                                <template #after><span class="text-onsurface-variant/50 pr-4">deg</span></template>
                            </VInputSlider>
                        </div>
                    </section>

                    <section>
                        <h2 class="text-secondary mb-4 font-bold">Wahrer Wind</h2>
                        <div class="mb-4">
                            <VInputNumber
                                label="Wahre Windgeschwindigkeit"
                                :model-value="absoluteWindSpeed"
                                @update:model-value="recalculateFromAbsoluteWind($event, absoluteWindDirection)"
                            >
                                <template #after><span class="text-onsurface-variant/50 pr-4">kn</span></template>
                            </VInputNumber>
                        </div>
                        <div class="mb-4">
                            <VInputSlider
                                label="Wahre Windrichtung"
                                :model-value="absoluteWindDirection"
                                :min="0"
                                :max="360"
                                @update:model-value="recalculateFromAbsoluteWind(absoluteWindSpeed, $event)"
                            >
                                <template #after><span class="text-onsurface-variant/50 pr-4">deg</span></template>
                            </VInputSlider>
                        </div>
                    </section>
                </div>
                <section class="grow">
                    <div class="bg-surface-container-low relative flex min-h-[85vh] items-stretch rounded-2xl lg:min-h-[75vh]">
                        <div class="absolute top-4 right-4 flex gap-2">
                            <button class="btn-secondary" @click="scale -= 2">-</button>
                            <button class="btn-secondary" @click="scale += 2">+</button>
                        </div>
                        <svg ref="chart" xmlns="http://www.w3.org/2000/svg" width="100%">
                            <g :transform="`translate(${dx}, ${dy})`">
                                <line x1="-3000" y1="0" x2="3000" y2="0" stroke-width="1px" class="stroke-onsurface-variant opacity-10" />
                                <line x1="0" y1="-3000" x2="0" y2="3000" stroke-width="1px" class="stroke-onsurface-variant opacity-10" />
                                <PointerArrow :pointer="course" :dash="4" arrow-end class="text-primary" />
                                <PointerArrow :pointer="heading" :dash="4" class="text-onsurface-variant opacity-50" />
                                <PointerArrow :pointer="movementWind" arrow-end class="text-primary" />
                                <PointerArrow :pointer="relativeWind" arrow-start class="text-secondary" />
                                <PointerArrow :pointer="absoluteWind" arrow-start class="text-error" />
                            </g>
                        </svg>
                        <div class="absolute right-4 bottom-4 text-sm">
                            <div class="flex items-center gap-2">
                                <hr class="border-primary my-0 w-4 border border-dashed" />
                                <span class="text-primary">Kurs über Grund</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <hr class="border-onsurface-variant my-0 w-4 border border-dashed opacity-50" />
                                <span class="text-onsurface-variant">Heading</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <hr class="border-primary my-0 w-4 border" />
                                <span class="text-primary">Fahrtwind</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <hr class="border-secondary my-0 w-4 border" />
                                <span class="text-secondary">Scheinbarer Wind</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <hr class="border-error my-0 w-4 border" />
                                <span class="text-error">Wahrer Wind</span>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { VInputNumber, VInputSlider } from '@/ui/components/common';
import type { Pointer } from '@/ui/views/wind-calculator/Pointer.ts';
import { calculatePointerDirection } from '@/ui/views/wind-calculator/Pointer.ts';
import { calculatePointer } from '@/ui/views/wind-calculator/Pointer.ts';
import PointerArrow from '@/ui/views/wind-calculator/PointerArrow.vue';

type RouteEmits = (e: 'update:tab-title', value: string) => void;
const emit = defineEmits<RouteEmits>();

const hdg = ref<number>(310);
const cog = ref<number>(315);
const sog = ref<number>(6.8);
const relativeWindSpeed = ref<number>(10);
const relativeWindDirection = ref<number>(130);
const absoluteWindSpeed = ref<number>(0);
const absoluteWindDirection = ref<number>(0);
const scale = ref<number>(20);
const chart = ref<SVGElement | null>(null);

const relativeWind = ref<Pointer>({ x1: 0, y1: 0, x2: 0, y2: 0 });
const absoluteWind = ref<Pointer>({ x1: 0, y1: 0, x2: 0, y2: 0 });
const course = computed<Pointer>(() => calculatePointer({ x: 0, y: 0 }, cog.value, sog.value * scale.value));
const heading = computed<Pointer>(() => calculatePointer({ x: 0, y: 0 }, hdg.value, 1000));
const movementWind = computed<Pointer>(() => calculatePointer({ x: 0, y: 0 }, cog.value, -1 * sog.value * scale.value));

const dx = computed<number>(() => {
    const xValues = [
        course.value.x1,
        course.value.x2,
        movementWind.value.x1,
        movementWind.value.x2,
        relativeWind.value.x1,
        relativeWind.value.x2,
        absoluteWind.value.x1,
        absoluteWind.value.x2,
    ].filter((it) => !Number.isNaN(it));
    const xMin = Math.round(xValues.reduce((min, it) => Math.min(min, it), 0));
    const xMax = Math.round(xValues.reduce((max, it) => Math.max(max, it), 0));
    const wSVG = chart.value?.clientWidth ?? 0;
    const wContent = xMax - xMin;
    return Math.abs(xMin) + (wSVG - wContent) / 2;
});
const dy = computed<number>(() => {
    const yValues = [
        course.value.y1,
        course.value.y2,
        movementWind.value.y1,
        movementWind.value.y2,
        relativeWind.value.y1,
        relativeWind.value.y2,
        absoluteWind.value.y1,
        absoluteWind.value.y2,
    ].filter((it) => !Number.isNaN(it));
    const yMin = Math.round(yValues.reduce((min, it) => Math.min(min, it), 0));
    const yMax = Math.round(yValues.reduce((max, it) => Math.max(max, it), 0));
    const hSVG = chart.value?.clientHeight ?? 0;
    const hContent = yMax - yMin;
    return Math.abs(yMin) + (hSVG - hContent) / 2;
});

function init(): void {
    emit('update:tab-title', 'Windrechner');
    watch(movementWind, recalculate);
    recalculate();
}

function recalculate(): void {
    if (absoluteWindSpeed.value === 0 && absoluteWindDirection.value === 0) {
        recalculateFromRelativeWind(relativeWindSpeed.value, relativeWindDirection.value);
    } else {
        recalculateFromAbsoluteWind(absoluteWindSpeed.value, absoluteWindDirection.value);
    }
}

function recalculateFromRelativeWind(speed: number | string, direction: number | string): void {
    relativeWindSpeed.value = Number(speed);
    relativeWindDirection.value = normalizeAngle(Number(direction), -180, 180);

    relativeWind.value = calculatePointer(
        { x: movementWind.value.x2, y: movementWind.value.y2 },
        cog.value + relativeWindDirection.value,
        relativeWindSpeed.value * scale.value
    );
    absoluteWind.value = { x1: 0, y1: 0, x2: relativeWind.value.x2, y2: relativeWind.value.y2 };

    const x = Math.abs(absoluteWind.value.x2 - absoluteWind.value.x1) / scale.value;
    const y = Math.abs(absoluteWind.value.y2 - absoluteWind.value.y1) / scale.value;
    absoluteWindSpeed.value = Math.round(Math.sqrt(x * x + y * y) * 10) / 10;
    absoluteWindDirection.value = normalizeAngle(calculatePointerDirection(absoluteWind.value));
}

function recalculateFromAbsoluteWind(speed: number | string, direction: number | string): void {
    absoluteWindSpeed.value = Number(speed);
    absoluteWindDirection.value = normalizeAngle(Number(direction));

    absoluteWind.value = calculatePointer({ x: 0, y: 0 }, absoluteWindDirection.value, absoluteWindSpeed.value * scale.value);
    relativeWind.value = { x1: movementWind.value.x2, y1: movementWind.value.y2, x2: absoluteWind.value.x2, y2: absoluteWind.value.y2 };

    const x = Math.abs(relativeWind.value.x2 - relativeWind.value.x1) / scale.value;
    const y = Math.abs(relativeWind.value.y2 - relativeWind.value.y1) / scale.value;
    relativeWindSpeed.value = Math.round(Math.sqrt(x * x + y * y) * 10) / 10;
    relativeWindDirection.value = normalizeAngle(calculatePointerDirection(relativeWind.value) - cog.value, -180, 180);
}

function normalizeAngle(a: number, min: number = 0, max: number = 360): number {
    let r = a;
    while (r > max) r -= 360;
    while (r < min) r += 360;
    return r;
}

init();
</script>
