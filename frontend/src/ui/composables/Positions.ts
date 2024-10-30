import { computed, ref } from 'vue';
import type { InputSelectOption, Position, PositionKey } from '@/domain';
import { usePositionUseCase } from '@/ui/composables/Application';

export function usePositions() {
    const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());

    const positionUseCase = usePositionUseCase();

    async function update(): Promise<void> {
        const pos = await positionUseCase.getPositions();
        positions.value.clear();
        pos.forEach((p) => positions.value.set(p.key, p));
    }

    const options = computed<InputSelectOption<string | undefined>[]>(() => {
        const positionOptions: InputSelectOption<string | undefined>[] = [...positions.value.values()]
            .sort((a, b) => b.prio - a.prio)
            .map((it) => ({
                label: it.name,
                value: it.key,
            }));
        return positionOptions;
    });

    const optionsIncludingNone = computed<InputSelectOption<string | undefined>[]>(() => {
        const positionOptions = options.value;
        positionOptions.unshift({ value: undefined, label: '-' });
        return positionOptions;
    });

    const all = computed<Position[]>(() => {
        return [...positions.value.values()].sort((a, b) => b.prio - a.prio);
    });

    function get(positonKey: PositionKey): Position {
        return (
            positions.value.get(positonKey) || {
                key: positonKey,
                color: '',
                prio: 0,
                name: positonKey,
            }
        );
    }

    update();

    return { positions, options, optionsIncludingNone, get, update, all };
}
