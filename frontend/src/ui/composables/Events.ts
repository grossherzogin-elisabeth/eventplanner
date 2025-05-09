import { ref } from 'vue';
import type { Event } from '@/domain';
import { useEventUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import type { Selectable } from '@/ui/model/Selectable.ts';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useEvents() {
    const useCase = useEventUseCase();
    const service = useEventService();
    const yearsLoaded = ref<number[]>([]);
    const events = ref<(Event & Selectable)[] | undefined>(undefined);

    async function fetch(...year: number[]): Promise<void> {
        if (year.length > 0) {
            yearsLoaded.value = year;
        }
        events.value = undefined;
        if (yearsLoaded.value.length > 0) {
            let temp: Event[] = [];
            for (const y of yearsLoaded.value) {
                temp = temp.concat(await useCase.getEvents(y));
            }
            events.value = temp;
        } else {
            yearsLoaded.value = [];
        }
    }

    function selectNone(): void {
        events.value?.forEach((it) => (it.selected = false));
    }

    function selectAll(): void {
        events.value?.forEach((it) => (it.selected = true));
    }

    return {
        useCase,
        service,
        events,
        fetch,
        selectAll,
        selectNone,
    };
}
