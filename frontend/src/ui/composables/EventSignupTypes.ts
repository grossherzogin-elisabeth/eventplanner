import type { ComputedRef } from 'vue';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';
import { EventSignupType } from '@/domain';

export interface UseEventSignupTypes {
    options: ComputedRef<InputSelectOption<EventSignupType>[]>;
    optionsIncludingNone: ComputedRef<InputSelectOption<EventSignupType | undefined>[]>;
    getName(locale: string): string;
}

export function useEventSignupTypes(): UseEventSignupTypes {
    const { t } = useI18n();

    const options = computed<InputSelectOption<EventSignupType>[]>(() => {
        return [EventSignupType.Assignment, EventSignupType.Open].map((it) => ({
            value: it,
            label: t(`domain.event-signup-type.${it}`),
        }));
    });

    const optionsIncludingNone = computed<InputSelectOption<EventSignupType | undefined>[]>(() => {
        const result: InputSelectOption<EventSignupType | undefined>[] = options.value;
        result.unshift({ value: undefined, label: '-' });
        return result;
    });

    function getName(type: EventSignupType): string {
        return options.value.find((it) => it.value === type)?.label || type;
    }

    return { options, optionsIncludingNone, getName };
}
