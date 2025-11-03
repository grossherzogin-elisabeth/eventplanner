<template>
    <div class="xs:p-8 p-4">
        <div class="grid grid-cols-4 gap-4">
            <div class="flex flex-col">
                <div class="demo">
                    <div class="bg-primary text-onprimary">primary</div>
                    <div class="bg-onprimary text-primary">on primary</div>
                </div>
                <div class="demo">
                    <div class="bg-primary-container text-onprimary-container">primary container</div>
                    <div class="bg-onprimary-container text-primary-container">on primary container</div>
                </div>
                <div class="demo">
                    <div class="bg-primary-fixed text-onprimary-fixed">primary fixed</div>
                    <div class="bg-onprimary-fixed text-primary-fixed">on primary fixed</div>
                </div>
                <div class="demo">
                    <div class="bg-primary-variant text-onprimary-variant">primary variant</div>
                    <div class="bg-onprimary-variant text-primary-variant">on primary variant</div>
                </div>
            </div>
            <div class="flex flex-col">
                <div class="demo">
                    <div class="bg-secondary text-onsecondary">secondary</div>
                    <div class="bg-onsecondary text-secondary">on secondary</div>
                </div>
                <div class="demo">
                    <div class="bg-secondary-container text-onsecondary-container">secondary container</div>
                    <div class="bg-onsecondary-container text-secondary-container">on secondary container</div>
                </div>
                <div class="demo">
                    <div class="bg-secondary-fixed text-onsecondary-fixed">secondary fixed</div>
                    <div class="bg-onsecondary-fixed text-secondary-fixed">on secondary fixed</div>
                </div>
                <div class="demo">
                    <div class="bg-secondary-variant text-onsecondary-variant">secondary variant</div>
                    <div class="bg-onsecondary-variant text-secondary-variant">on secondary variant</div>
                </div>
            </div>
            <div class="flex flex-col">
                <div class="demo">
                    <div class="bg-tertiary text-ontertiary">tertiary</div>
                    <div class="bg-ontertiary text-tertiary">on tertiary</div>
                </div>
                <div class="demo">
                    <div class="bg-tertiary-container text-ontertiary-container">tertiary container</div>
                    <div class="bg-ontertiary-container text-tertiary-container">on tertiary container</div>
                </div>
                <div class="demo">
                    <div class="bg-tertiary-fixed text-ontertiary-fixed">tertiary fixed</div>
                    <div class="bg-ontertiary-fixed text-tertiary-fixed">on tertiary fixed</div>
                </div>
                <div class="demo">
                    <div class="bg-tertiary-variant text-ontertiary-variant">tertiary variant</div>
                    <div class="bg-ontertiary-variant text-tertiary-variant">on tertiary variant</div>
                </div>
            </div>
            <div class="flex flex-col">
                <div class="demo">
                    <div class="bg-error text-onerror">error</div>
                    <div class="bg-onerror text-error">on error</div>
                </div>
                <div class="demo">
                    <div class="bg-error-container text-onerror-container">error container</div>
                    <div class="bg-onerror-container text-error-container">on error container</div>
                </div>
            </div>
            <div class="col-span-3 grid grid-cols-3 gap-4">
                <div class="demo">
                    <div class="bg-surface-dim text-onsurface">surface dim</div>
                </div>
                <div class="demo">
                    <div class="bg-surface text-onsurface">surface</div>
                </div>
                <div class="demo">
                    <div class="bg-surface-bright text-onsurface">surface bright</div>
                </div>
            </div>
            <div class="col-span-3 grid grid-cols-5 gap-4">
                <div class="demo">
                    <div class="bg-surface-container-lowest text-onsurface">surface container lowest</div>
                </div>
                <div class="demo">
                    <div class="bg-surface-container-low text-onsurface">surface container low</div>
                </div>
                <div class="demo">
                    <div class="bg-surface-container text-onsurface">surface container</div>
                </div>
                <div class="demo">
                    <div class="bg-surface-container-high text-onsurface">surface container high</div>
                </div>
                <div class="demo">
                    <div class="bg-surface-container-highest text-onsurface">surface container highest</div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup></script>
<style scoped>
@reference "tailwindcss"

.demo {
    @apply mb-4;
    font-size: var(--text-sm);
    font-weight: var(--font-weight-bold);
}

.demo > div {
    display: flex;
    align-items: center;
    @apply h-24;
    @apply px-4;
}

.demo > div:last-child {
    @apply h-10;
}

.demo > div:first-child {
    @apply h-24;
}
</style>
