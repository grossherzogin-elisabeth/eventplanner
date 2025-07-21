import { computed } from 'vue';
import type { InputSelectOption } from '@/domain';
import { EventSignupType } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEventSignupTypes() {
    const options = computed<InputSelectOption<EventSignupType>[]>(() => {
        return [
            { value: EventSignupType.Assignment, label: 'Offene Anmeldung mit manueller Einteilung' },
            { value: EventSignupType.Open, label: 'Offene Teilnahme' },
        ];
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
