<template>
    <component :is="flag" class="flag-icon"></component>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import be from 'svg-country-flags/svg/be.svg?component';
import de from 'svg-country-flags/svg/de.svg?component';
import dk from 'svg-country-flags/svg/dk.svg?component';
import fi from 'svg-country-flags/svg/fi.svg?component';
import en from 'svg-country-flags/svg/gb.svg?component';
import nl from 'svg-country-flags/svg/nl.svg?component';
import no from 'svg-country-flags/svg/no.svg?component';
import pl from 'svg-country-flags/svg/pl.svg?component';
import se from 'svg-country-flags/svg/se.svg?component';

interface Props {
    country: string;
}

const props = defineProps<Props>();

const flag = computed(() => {
    switch (props.country) {
        case 'de':
            return de;
        case 'en':
            return en;
        case 'gb':
            return en;
        case 'nl':
            return nl;
        case 'be':
            return be;
        case 'dk':
            return dk;
        case 'fi':
            return fi;
        case 'se':
            return se;
        case 'no':
            return no;
        case 'pl':
            return pl;
        default:
    }
    return undefined;
});
</script>

<style>
.flag-icon {
    display: inline;
    max-height: 1em;
    vertical-align: -0.125em;
    overflow: visible;
}
</style>
