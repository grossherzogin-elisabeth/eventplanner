<template>
    <div class="h-full overflow-y-auto px-8 pt-8 pb-8 md:px-16 xl:px-20">
        <h1 class="mb-4">Verantwortlich für den Inhalt</h1>
        <p class="mb-4">Angaben gemäß § 5 TMG</p>
        <p class="mb-4">
            Schulschiffverein „Großherzogin Elisabeth“ e. V.<br />
            Rathausplatz 5<br />
            26931 Elsfleth<br />
            Deutschland
        </p>
        <p class="mb-4">
            Eingetragen im Vereinsregister.<br />
            Registernummer: 100123<br />
            Registergericht: Amtsgericht Oldenburg<br />
        </p>
        <p class="mb-4">
            Vertreten durch den Vorstand:<br />
            Kapitän Johannes Reifig<br />
        </p>
        <h1 class="mt-8 mb-4">Kontakt</h1>
        <p class="mb-4">
            Telefon: +49 (0) 4404 988 672<br />
            Telefax: +49 (0) 4404 988 673<br />
            E-Mail: office@grossherzogin-elisabeth.de
        </p>
        <h1 class="mt-8 mb-4">Verantwortlich für den Inhalt nach § 55 Abs. 2 RStV</h1>
        <p class="mb-4">Kapitän Johannes Reifig</p>
    </div>
</template>
<script lang="ts" setup>
type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

function init(): void {
    emit('update:tab-title', 'Impressum');
}

init();
</script>
