<template>
    <div class="xl:overflow-auto">
        <div class="flex px-8 pb-8 md:px-16 xl:px-20">
            <div class="w-full xl:max-w-2xl">
                <div
                    class="sticky top-12 z-10 -mx-4 flex h-14 justify-end bg-surface pb-2 pt-4 xl:top-0 xl:h-16 xl:pt-8"
                ></div>
                <div class="-mx-4 rounded-2xl bg-secondary-container p-8 text-onsecondary-container">
                    <h3 class="mb-4">Dein Account konnte nicht verknüpft werden</h3>
                    <p class="mb-4">
                        Dein Account konnte nicht automatisch einem Stammcrew Mitglied zugeordnet werden. Das kann daran
                        liegen, das du dich mit einer unbekannten eMail Adresse registriert hast, oder dein angegebener
                        Name von dem in der Kaderliste abweicht. Aus Datenschutgründen können dir deshalb noch keine
                        Reisen angezeigt werden.
                    </p>
                    <p class="mb-4">
                        Dieser Fehler kann aktuell leider noch nicht automatisch aufgelöst werden. Wir arbeiten an einer
                        Lösung. Bitte habe noch etwas Gedult und kontaktiere für die An- und Abmeldung von Reisen
                        vorerst weiterhin direkt das Büro.
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

function init(): void {
    emit('update:title', 'Start');
}

init();
</script>
