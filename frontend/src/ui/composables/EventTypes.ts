import { computed } from 'vue';
import type { InputSelectOption } from '@/domain';
import { EventType } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEventTypes() {
    const options = computed<InputSelectOption<EventType>[]>(() => {
        return [
            { value: EventType.SingleDayEvent, label: 'Tagesfahrt' },
            { value: EventType.WeekendEvent, label: 'Wochenendreise' },
            { value: EventType.MultiDayEvent, label: 'Mehrtagesfahrt' },
            { value: EventType.WorkEvent, label: 'Arbeitsdiesnt' },
        ];
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
