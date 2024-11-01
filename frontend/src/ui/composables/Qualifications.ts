import { computed, ref } from 'vue';
import type { InputSelectOption, Qualification, QualificationKey } from '@/domain';
import { useQualificationsUseCase } from '@/ui/composables/Application';

export function useQualifications() {
    const qualifications = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());

    const qualificationsUseCase = useQualificationsUseCase();

    async function update(): Promise<void> {
        const pos = await qualificationsUseCase.getQualifications();
        qualifications.value.clear();
        pos.forEach((p) => qualifications.value.set(p.key, p));
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
        return [...qualifications.value.values()].sort((a, b) => a.name.localeCompare(b.name));
    });

    function get(qualificationKey: QualificationKey): Qualification {
        return (
            qualifications.value.get(qualificationKey) || {
                key: qualificationKey,
                name: qualificationKey,
                icon: '',
                description: '',
                expires: false,
                grantsPositions: [],
            }
        );
    }

    update();

    return { qualifications, options, optionsIncludingNone, get, update, all };
}
