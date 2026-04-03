import type { ComputedRef, Ref } from 'vue';
import { computed, ref } from 'vue';
import { usePositionUseCase } from '@/application';
import type { InputSelectOption, Position, PositionKey } from '@/domain';

export interface UsePositions {
    positions: Ref<Map<PositionKey, Position>>;
    options: ComputedRef<InputSelectOption<PositionKey | undefined>[]>;
    optionsIncludingNone: ComputedRef<InputSelectOption<PositionKey | undefined>[]>;
    get(positionKey?: PositionKey): Position;
    update(): Promise<void>;
    all: ComputedRef<Position[]>;
    loading: Promise<void>;
}

export function usePositions(): UsePositions {
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
