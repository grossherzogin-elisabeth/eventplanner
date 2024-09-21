<template>
    <div class="h-full overflow-y-auto px-8 pb-8 pt-8 md:px-16 xl:px-20">
        <div class="max-w-2xl">
            <h1 class="mb-4">Die verlorene Seite</h1>
            <p class="mb-4">
                Oh oh! Scheint, als ob wir im digitalen Dschungel verloren gegangen sind! Die Seite, nach der du suchst,
                ist wie ein verschollener Schatz - nirgendwo zu finden!
            </p>
            <p class="mb-4">
                Entweder haben wir unsere digitale Landkarte verloren oder du hast eine falsche Route genommen.
                Vielleicht solltest du deine Tastatur mit einem Kompass austauschen oder deinen Browser mit etwas
                magischem Feenstaub bestreuen.
            </p>
            <p class="mb-4">
                Aber keine Panik! Während wir uns auf die Suche nach der verlorenen Seite machen, warum stöberst du
                nicht durch unsere anderen digitalen Schätze? Wer weiß, vielleicht entdeckst du etwas noch Besseres als
                das, was du gesucht hast!
            </p>
            <p class="mb-4">
                Wir entschuldigen uns für die Unannehmlichkeiten. Aber hey, zumindest haben wir jetzt eine spannende
                Geschichte zu erzählen, oder? Wenn du immer noch auf der Suche nach dem verlorenen Schatz bist,
                kontaktiere uns gerne - wir werden unsere digitalen Piraten bemühen, um dir zu helfen!
            </p>
            <p class="mb-4">Danke für deine Geduld und für die gemeinsame Suche nach dem verlorenen Code!</p>
        </div>
        <div class="mt-8 flex">
            <RouterLink class="btn-primary" to="/">
                <span>Zurück zur Startseite</span>
            </RouterLink>
        </div>
    </div>
</template>
<script lang="ts" setup>
interface RouteEmits {
    (e: 'update:title', value: string): void;
}

const emit = defineEmits<RouteEmits>();

function init(): void {
    emit('update:title', '404 - Seite nicht gefunden');
}

init();
</script>
