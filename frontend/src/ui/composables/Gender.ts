import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useGender() {
    const { t } = useI18n();

    const options: InputSelectOption<string>[] = ['m', 'f', 'd'].map((it) => ({
        value: it,
        label: t(`generic.gender.${it}`),
    }));

    function getName(type: string): string {
        return options.find((it) => it.value === type)?.label || type;
    }

    return { options, getName };
}
