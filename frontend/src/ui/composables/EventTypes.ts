import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';
import { EventType } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEventTypes() {
    const { t } = useI18n();

    const options = computed<InputSelectOption<EventType>[]>(() => {
        return [
            EventType.SingleDayEvent,
            EventType.WeekendEvent,
            EventType.MultiDayEvent,
            EventType.WorkEvent,
            EventType.TrainingEvent,
            EventType.Other,
        ].map((it) => ({
            value: it,
            label: t(`generic.event-type.${it}`),
        }));
    });

    const optionsIncludingNone = computed<InputSelectOption<EventType | undefined>[]>(() => {
        const result: InputSelectOption<EventType | undefined>[] = options.value;
        result.unshift({ value: undefined, label: '-' });
        return result;
    });

    function getName(type: EventType): string {
        return options.value.find((it) => it.value === type)?.label || type;
    }

    return { options, optionsIncludingNone, getName };
}
