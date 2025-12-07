<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-paint-brush"
        :label="$t('views.account.app-settings.color')"
        direct
        :validate="validateColor"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.color">
                {{ props.modelValue.color }}
            </span>
            <span v-else> #188edc </span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                Du kannst das Design der Anwendung personalisieren, indem du eine eigene Primärfarbe auswählst. Aus dieser Primärfarbe wird
                automatisch eine vollständige, harmonische und barrierefreie Farbpalette generiert.
            </p>
            <div class="-mx-[0.385rem] mb-8 flex flex-wrap items-center gap-2">
                <button
                    v-for="theme in defaultThemes"
                    :key="theme.color"
                    class="hover:border-primary rounded-full border-[0.125rem] p-[0.25rem]"
                    :class="theme.color === value.color ? 'border-primary' : 'border-transparent'"
                    @click="value.color = theme.color"
                >
                    <div class="grid h-16 w-16 grid-cols-2 overflow-hidden rounded-full">
                        <span class="col-span-full block h-8 dark:hidden" :style="{ backgroundColor: theme.light.primary }"></span>
                        <span class="block h-8 dark:hidden" :style="{ backgroundColor: theme.light.secondary }"></span>
                        <span class="block h-8 dark:hidden" :style="{ backgroundColor: theme.light.tertiary }"></span>

                        <span class="col-span-full hidden h-8 dark:block" :style="{ backgroundColor: theme.dark.primary }"></span>
                        <span class="hidden h-8 dark:block" :style="{ backgroundColor: theme.dark.secondary }"></span>
                        <span class="hidden h-8 dark:block" :style="{ backgroundColor: theme.dark.tertiary }"></span>
                    </div>
                </button>
            </div>
            <VInputText
                v-model="value.color"
                :label="$t('views.account.app-settings.color')"
                :errors="errors['color']"
                :errors-visible="true"
            />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { COLOR_REGEX } from '@/application/utils/RegExpresions.ts';
import { Validator, matchesPattern, notEmpty } from '@/common/validation';
import type { UserSettings } from '@/domain';
import { VInputText, VInteractiveListItem } from '@/ui/components/common';
import { Hct, SchemeTonalSpot, argbFromHex, hexFromArgb } from '@material/material-color-utilities';

interface Props {
    modelValue: UserSettings;
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const colors = computed<string[]>(() => {
    const defaultColors = ['#188EDC', '#166534', '#c2410c', '#9333ea'];
    if (props.modelValue.color && !defaultColors.includes(props.modelValue.color)) {
        return [props.modelValue.color, ...defaultColors];
    }
    return defaultColors;
});

const defaultThemes = computed(() =>
    colors.value.map((baseColor) => {
        const argb = argbFromHex(baseColor);
        const light = new SchemeTonalSpot(Hct.fromInt(argb), false, 0);
        const dark = new SchemeTonalSpot(Hct.fromInt(argb), true, 0);
        return {
            color: baseColor,
            dark: {
                primary: hexFromArgb(light.primary),
                secondary: hexFromArgb(light.secondary),
                tertiary: hexFromArgb(light.tertiary),
            },
            light: {
                primary: hexFromArgb(dark.primary),
                secondary: hexFromArgb(dark.secondary),
                tertiary: hexFromArgb(dark.tertiary),
            },
        };
    })
);

function validateColor(userSettings: UserSettings): Record<string, string[]> {
    return Validator.validate(
        'color',
        userSettings.color,
        notEmpty(),
        matchesPattern(COLOR_REGEX, 'Bitte gib eine Farbe im Format #FFFFFF an')
    ).getErrors();
}
</script>
