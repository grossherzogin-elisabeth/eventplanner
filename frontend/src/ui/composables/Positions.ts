import { computed, ref } from 'vue';
import type { InputSelectOption, Position, PositionKey } from '@/domain';
import { usePositionUseCase } from '@/ui/composables/Application';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function usePositions() {
    const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());

    const positionUseCase = usePositionUseCase();

    async function update(): Promise<void> {
        const pos = await positionUseCase.getPositions();
        positions.value.clear();
        pos.forEach((p) => positions.value.set(p.key, p));
    }

    const options = computed<InputSelectOption<PositionKey | undefined>[]>(() => {
        const positionOptions: InputSelectOption<PositionKey | undefined>[] = [...positions.value.values()]
            .sort((a, b) => b.prio - a.prio)
            .map((it) => ({
                label: it.name,
                value: it.key,
            }));
        return positionOptions;
    });

    const optionsIncludingNone = computed<InputSelectOption<PositionKey | undefined>[]>(() => {
        const positionOptions = options.value;
        positionOptions.unshift({ value: undefined, label: '-' });
        return positionOptions;
    });

    const all = computed<Position[]>(() => {
        return [...positions.value.values()].sort((a, b) => b.prio - a.prio);
    });

    function get(positionKey?: PositionKey): Position {
        return (
            positions.value.get(positionKey ?? '') || {
                key: positionKey ?? '',
                color: '',
                prio: 0,
                name: positionKey ?? '',
                imoListRank: positionKey ?? '',
            }
        );
    }

    const loading = update();

    return { positions, options, optionsIncludingNone, get, update, all, loading };
}
