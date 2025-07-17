<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <div class="px-4 pb-8 pt-8 xs:px-8 md:px-16 xl:px-20">
            <div class="max-w-2xl">
                <h1 class="mb-4 font-bold">Dein Account konnte nicht zugeordnet werden</h1>
                <p class="mb-4">
                    Um Zugriff auf die Daten der App zu erhalten, musst du ein Stammcrew Mitglied sein. Wir konnten deinen Account
                    allerdings keinem registrierten Stammcrew Mitglied zuordnen. Dies kann verschiedene Gründe haben:
                </p>
                <ul class="mb-4 ml-4 list-disc space-y-2">
                    <li>Du hast bei der Registrierung eine andere Email Adresse als die uns bekannte verwendet</li>
                    <li>
                        Du hast bei der Registrierung deinen Namen etwas anders geschrieben (z.B. mit/ohne Zwischennamen, mit/ohne
                        Bindestrich, ...)
                    </li>
                    <li>Du bist dem Verein noch nicht beigetreten und bist deshalb noch nicht in unserer Datenbank hinterlegt</li>
                </ul>
                <section>
                    <VInfo class="my-8">
                        <h2 class="mb-4 mt-1 text-base font-bold">Noch Kein Stammcrew Mitglied?</h2>
                        <p class="mb-4 text-base">
                            Solltest du noch kein Vereinsmitglied sein, kannst du auf unserer Website einen Mitgliedsantrag stellen. Du
                            bekommst innerhalb einiger Werktage eine Bestätigung und kannst die App dann vollumfänglich nutzen.
                        </p>
                        <div class="hidden justify-center text-base 2xl:flex">
                            <a href="https://www.grossherzogin-elisabeth.de/mitglied-werden/" class="btn-primary">
                                <span class="px-4"> Jetzt Mitglied werden! </span>
                            </a>
                        </div>
                    </VInfo>
                </section>
                <p class="mb-4">
                    Wenn du schon Vereinsmitglied bist und trotzdem diese Seite siehst, gab es vermutlich einen der oben genannten Fehler
                    bei deiner Registrierung. Bitte melde dich bei
                    <a :href="`mailto:${config.technicalSupportEmail}`" class="link">{{ config.technicalSupportEmail }}</a>
                    für technischen Support.
                </p>
            </div>
        </div>
        <div class="pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden">
            <a href="https://www.grossherzogin-elisabeth.de/mitglied-werden/" class="btn-floating pointer-events-auto">
                <i class="fa-solid fa-edit"></i>
                <span> Jetzt Mitglied werden! </span>
            </a>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { Role } from '@/domain';
import { VInfo } from '@/ui/components/common';
import { useAuthUseCase, useConfig } from '@/ui/composables/Application.ts';
import { Routes } from '@/ui/views/Routes.ts';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const router = useRouter();
const config = useConfig();
const auth = useAuthUseCase();
const signedInUser = auth.getSignedInUser();

function init(): void {
    emit('update:tab-title', 'Start');
    if (signedInUser.roles.includes(Role.TEAM_MEMBER)) {
        router.push({ name: Routes.Home });
    } else if (signedInUser.roles.includes(Role.USER_MANAGER)) {
        router.push({ name: Routes.UsersList });
    } else if (signedInUser.roles.includes(Role.EVENT_PLANNER)) {
        router.push({ name: Routes.EventsListAdmin });
    }
}

init();
</script>
