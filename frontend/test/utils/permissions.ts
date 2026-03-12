import { useAuthService } from '@/application';
import type { Permission, SignedInUser } from '@/domain';
import { mockSignedInUser } from '~/mocks';

export function setupUserPermissions(permissions: Permission[]): SignedInUser {
    const authService = useAuthService();
    const signedInUser = mockSignedInUser();
    signedInUser.permissions.push(...permissions);
    authService.setSignedInUser(signedInUser);
    return signedInUser;
}
