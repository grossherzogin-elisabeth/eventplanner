<template>
    <template v-if="props.users">
        <hr />
        <li data-test-id="action-contact" class="permission-read-user-details context-menu-item" @click="emit('contact', props.users)">
            <i class="fa-solid fa-envelope" />
            <span>{{ $t('domain.user.actions.write-email') }}</span>
        </li>
        <template v-if="props.users.length === 1">
            <li
                data-test-id="action-impersonate"
                class="permission-read-user-details context-menu-item"
                @click="emit('impersonate', props.users[0])"
            >
                <i class="fa-solid fa-user-secret" />
                <span>{{ $t('domain.user.actions.impersonate') }}</span>
            </li>
            <li
                data-test-id="action-create-registration"
                class="permission-create-registrations context-menu-item"
                @click="emit('create-registration', props.users[0])"
            >
                <i class="fa-solid fa-calendar-plus" />
                <span>{{ $t('domain.registration.actions.create') }}</span>
            </li>
            <li
                data-test-id="action-edit"
                class="permission-update-user-details context-menu-item"
                @click="emit('edit', { user: props.users[0], event: $event })"
            >
                <i class="fa-solid fa-edit" />
                <span>{{ $t('generic.edit') }}</span>
            </li>
            <li
                data-test-id="action-delete"
                class="permission-delete-users context-menu-item text-error"
                @click="emit('delete', props.users[0])"
            >
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('generic.delete') }}</span>
            </li>
        </template>
    </template>
</template>

<script lang="ts" setup>
import type { User } from '@/domain';

interface Props {
    users?: User[];
}

interface Emits {
    (e: 'impersonate', user: User): void;
    (e: 'delete', user: User): void;
    (e: 'edit', payload: { user: User; event: MouseEvent }): void;
    (e: 'create-registration', user: User): void;
    (e: 'contact', users: User[]): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
