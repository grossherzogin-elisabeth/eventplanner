import { computed } from 'vue';
import type { InputSelectOption } from '@/domain';
import { EventState } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEventStates() {
    const options = computed<InputSelectOption<EventState>[]>(() => {
        return [
            { value: EventState.Draft, label: 'Entwurf' },
            { value: EventState.OpenForSignup, label: 'Crew Anmeldung' },
            { value: EventState.Planned, label: 'Veröffentlicht' },
            { value: EventState.Canceled, label: 'Abgesagt' },
        ];
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
