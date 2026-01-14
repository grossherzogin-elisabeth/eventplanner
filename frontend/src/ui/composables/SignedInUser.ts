import type { Ref } from 'vue';
import { onBeforeUnmount } from 'vue';
import { ref } from 'vue';
import { useAuthService } from '@/application';
import type { SignedInUser } from '@/domain';

export function useSignedInUser(): { signedInUser: Ref<SignedInUser | undefined> } {
    const authService = useAuthService();

    const signedInUser = ref<SignedInUser | undefined>(authService.getSignedInUser());
    const unregister = authService.onChange(() => (signedInUser.value = authService.getSignedInUser()));

    onBeforeUnmount(unregister);

    return { signedInUser };
}
