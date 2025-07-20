import { computed } from 'vue';
import type { InputSelectOption } from '@/domain';
import { EventAccessType } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEventAccessTypes() {
    const options = computed<InputSelectOption<EventAccessType>[]>(() => {
        return [
            { value: EventAccessType.Assignment, label: 'Offene Anmeldung mit manueller Einteilung' },
            { value: EventAccessType.Open, label: 'Offene Teilnahme' },
        ];
    });

    const optionsIncludingNone = computed<InputSelectOption<EventAccessType | undefined>[]>(() => {
        const result: InputSelectOption<EventAccessType | undefined>[] = options.value;
        result.unshift({ value: undefined, label: '-' });
        return result;
    });

    function getName(type: EventAccessType): string {
        return options.value.find((it) => it.value === type)?.label || type;
    }

    return { options, optionsIncludingNone, getName };
}
