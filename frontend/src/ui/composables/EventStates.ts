import { type ComputedRef, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';
import { EventState } from '@/domain';

export interface UseEventStates {
    options: ComputedRef<InputSelectOption<EventState>[]>;
    optionsIncludingNone: ComputedRef<InputSelectOption<EventState | undefined>[]>;
    getName(locale: string): string;
}

export function useEventStates(): UseEventStates {
    const { t } = useI18n();

    const options = computed<InputSelectOption<EventState>[]>(() => {
        return [EventState.Draft, EventState.OpenForSignup, EventState.Planned, EventState.Canceled].map((it) => ({
            value: it,
            label: t(`domain.event-state.${it}`),
        }));
    });

    const optionsIncludingNone = computed<InputSelectOption<EventState | undefined>[]>(() => {
        const result: InputSelectOption<EventState | undefined>[] = options.value;
        result.unshift({ value: undefined, label: '-' });
        return result;
    });

    function getName(type: EventState): string {
        return options.value.find((it) => it.value === type)?.label || type;
    }

    return { options, optionsIncludingNone, getName };
}
