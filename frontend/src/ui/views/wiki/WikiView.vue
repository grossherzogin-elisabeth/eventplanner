<template>
    <div
        class="sticky top-12 z-10 hidden bg-primary-50 px-4 py-4 md:pl-12 md:pr-16 md:pt-8 xl:top-0 xl:block xl:pl-16 xl:pr-20"
    >
        <div class="flex items-center space-x-4">
            <VInputText class="input-search w-96" placeholder="Wiki durchsuchen">
                <template #before>
                    <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                </template>
            </VInputText>
            <div class="hidden flex-grow md:block"></div>
            <div class="hidden items-stretch justify-end space-x-2 md:flex">
                <button class="btn-primary flex-grow whitespace-nowrap">
                    <i class="fa-solid fa-file-circle-plus"></i>
                    <span>Seite erstellen</span>
                </button>
            </div>
        </div>
    </div>
    <teleport to="#nav-right">
        <NavbarFilter placeholder="Wiki durchsuchen" />
    </teleport>
    <div class="h-full max-w-4xl px-8 pb-8 pt-6 md:px-16 xl:px-20">
        <h2 class="mb-4 text-xl">Der Rumpf</h2>
        <p class="mb-8">
            Der Rumpf ist der Schiffskörper der Lissi und wurde 1909 als Frachtsegler in noch komplett genieteter
            Bauweise erstellt. Ohne dieses Handbuch mit einer allzu detaillierten schiffbaulichen Beschreibung zu
            belasten, sollen doch einige wesentliche Konstruktionselemente sowie Abmaße erläutert werden.
        </p>
        <p class="mb-8">
            Der Rumpf ist, mit Ausnahme von Ausformung der Vor- und Achtersteven sowie des deutlichen Deckssprungs (das
            zu den Schiffsenden nach oben gebogene Hauptdeck), wie ein modernes Handelsschiff, also mit einem flachen
            Schiffsboden konstruiert. Er hat im Bereich des Hauptspants (d.h. in der Mitte des Rumpfes) einen fast
            rechteckigen Querschnitt für das Unterwasserschiff, was einem Schiff konstruktionsmäßig in Bezug auf den
            Tiefgang die größtmögliche Stabilität und Ladefähigkeit ermöglicht. Die Segeleigenschaften sind durch diese
            Konstruktion jedoch geringfügig negativ beeinflusst (mit hoher Windabdrift und schlechterer Kursstabilität).
        </p>
        <p class="mb-8">
            Die linke Seite des Rumpfs ist Backbord (nachfolgend Bb. abgekürzt) und die rechte Seite Steuerbord
            (nachfolgend Stb. abgekürzt). Der vordere Teil des Rumpfs ist der Bug (als Richtungsangabe: vorn) und der
            hintere Teil des Rumpfs ist das Heck (als Richtungsangabe: achtern).
        </p>
        <p class="mb-8">
            Die Ecke des Rumpfes, wo Schiffsseite und Schiffsboden zusammenlaufen, wird als Kimm bezeichnet. An der Kimm
            sind im mittleren Bereich des Rumpfes rechtwinklig abstehende Bleche angebracht. Diese werden Schlingerkiele
            genannt und sollen die Rollbewegungen des Schiffes dämpfen.
        </p>
    </div>
</template>
<script lang="ts" setup>
import { VInputText } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
</script>
