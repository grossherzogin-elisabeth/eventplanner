import { type ComputedRef, type Ref, computed, ref } from 'vue';
import { useQualificationsUseCase } from '@/application';
import type { InputSelectOption, Qualification, QualificationKey } from '@/domain';

export interface UseQualifications {
    map: Ref<Map<QualificationKey, Qualification>>;
    options: ComputedRef<InputSelectOption<QualificationKey | undefined>[]>;
    optionsIncludingNone: ComputedRef<InputSelectOption<QualificationKey | undefined>[]>;
    get(qualificationKey: QualificationKey): Qualification;
    update(): Promise<void>;
    all: ComputedRef<Qualification[]>;
    loading: Promise<void>;
}

export function useQualifications(): UseQualifications {
    const map = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());

    const qualificationsUseCase = useQualificationsUseCase();

    async function update(): Promise<void> {
        const qualifications = await qualificationsUseCase.getQualifications();
        map.value.clear();
        qualifications.forEach((p) => map.value.set(p.key, p));
    }

    const options = computed<InputSelectOption<string | undefined>[]>(() => {
        const qualificationOptions: InputSelectOption<string | undefined>[] = all.value.map((it) => ({
            label: it.name,
            value: it.key,
        }));
        return qualificationOptions;
    });

    const optionsIncludingNone = computed<InputSelectOption<string | undefined>[]>(() => {
        const qualificationOptions = options.value;
        qualificationOptions.unshift({ value: undefined, label: '-' });
        return qualificationOptions;
    });

    const all = computed<Qualification[]>(() => {
        return [...map.value.values()].sort((a, b) => a.name.localeCompare(b.name));
    });

    function get(qualificationKey: QualificationKey): Qualification {
        return (
            map.value.get(qualificationKey) || {
                key: qualificationKey,
                name: qualificationKey,
                icon: '',
                description: '',
                expires: false,
                grantsPositions: [],
            }
        );
    }

    const loading = update();

    return { map, options, optionsIncludingNone, get, update, all, loading };
}
