import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';

export interface UseDiet {
    options: InputSelectOption<string>[];
    getName(locale: string): string;
}
export function useDiet(): UseDiet {
    const { t } = useI18n();

    const options: InputSelectOption<string>[] = ['omnivore', 'vegetarian', 'vegan'].map((it) => ({
        value: it,
        label: t(`generic.diet.${it}`),
    }));

    function getName(type: string): string {
        return options.find((it) => it.value === type)?.label || type;
    }

    return { options, getName };
}
