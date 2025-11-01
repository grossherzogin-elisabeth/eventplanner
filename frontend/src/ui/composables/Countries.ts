import type { InputSelectOption } from '@/domain';
import countries from 'i18n-iso-countries';
import de from 'i18n-iso-countries/langs/de.json';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useCountries(favorites: string[] = []) {
    countries.registerLocale(de);

    const options: InputSelectOption[] = Object.keys(countries.getAlpha2Codes())
        .map((alpha2) => {
            const name = countries.getName(alpha2, 'de');
            return {
                value: alpha2,
                label: name || alpha2,
            };
        })
        .sort((a, b) => {
            const afi = favorites.indexOf(a.value);
            const bfi = favorites.indexOf(b.value);
            if (afi >= 0 || bfi >= 0) {
                if (afi === -1) return 1;
                if (bfi === -1) return -1;
                if (afi > bfi) return 1;
                if (bfi > afi) return -1;
                return 0;
            }
            return a.label.localeCompare(b.label);
        });

    function getName(locale: string): string {
        return countries.getName(locale, 'de') || locale;
    }

    return { options, getName };
}
