<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.account.app-settings.title') }}</h2>
            <VInteractiveList>
                <ThemeCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PreferredPositionCard
                    :model-value="props.modelValue"
                    :available-positions="user.positionKeys"
                    @update:model-value="emit('update:modelValue', $event)"
                />
                <VListItem icon="fa-user-tie" :label="$t('views.account.app-settings.roles')">
                    <template #default>
                        <p>
                            {{ signedInUser?.roles.map((r) => $t(`generic.role.${r}`)).join(', ') }}
                        </p>
                    </template>
                </VListItem>
                <VListItem icon="fa-list-check" :label="$t('views.account.app-settings.permissions')">
                    <template #default>
                        <ul class="font-mono">
                            <li v-for="permission in signedInUser?.permissions.sort()" :key="permission">
                                {{ permission }}
                            </li>
                        </ul>
                    </template>
                </VListItem>
            </VInteractiveList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { UserDetails, UserSettings } from '@/domain';
import { VListItem } from '@/ui/components/common';
import VInteractiveList from '@/ui/components/common/list/VInteractiveList.vue';
import { useSession } from '@/ui/composables/Session.ts';
import PreferredPositionCard from '@/ui/views/account/components/PreferredPositionCard.vue';
import ThemeCard from '@/ui/views/account/components/ThemeCard.vue';

interface Props {
    modelValue: UserSettings;
    user: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { signedInUser } = useSession();
</script>
