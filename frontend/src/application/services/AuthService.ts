import type { SignedInUser, UserDetails } from '@/domain';
import { Permission, Role } from '@/domain';
import { v4 as randomUUID } from 'uuid';

type Callback<T> = (t: T) => void;

export class AuthService {
    private loginListeners: Record<string, Callback<SignedInUser>> = {};
    private logoutListeners: Record<string, Callback<void>> = {};
    private changeListeners: Record<string, Callback<void>> = {};
    private signedInUser: SignedInUser | undefined = undefined;
    private impersonating: UserDetails | null = null;
    private offlineMode: boolean | undefined = undefined;

    constructor() {
        console.log('🚀 Initializing AuthService');
        this.signedInUser = this.loadStoredSignedInUser();
    }

    public getSignedInUser(): SignedInUser | undefined {
        if (this.signedInUser && this.impersonating) {
            return {
                ...this.signedInUser,
                key: this.impersonating.key,
                email: this.impersonating.email,
                gender: this.impersonating.gender,
                firstName: this.impersonating.firstName,
                lastName: this.impersonating.lastName,
                positions: this.impersonating.positionKeys,
                impersonated: true,
            };
        }
        return this.signedInUser;
    }

    public isOffline(): boolean {
        return this.offlineMode === true;
    }

    public setOffline(): void {
        this.offlineMode = true;
    }

    public setSignedInUser(signedInUser?: SignedInUser): void {
        this.offlineMode = false;
        if (signedInUser) {
            if (signedInUser.roles.includes(Role.ADMIN)) {
                signedInUser.permissions.push(Permission.BETA_FEATURES);
            }
            this.signedInUser = signedInUser;
            this.storeSignedInUser(signedInUser);
            this.notifyListeners(this.loginListeners, this.signedInUser);
        } else {
            this.signedInUser = undefined;
            this.notifyListeners(this.logoutListeners, undefined);
        }
        this.notifyListeners(this.changeListeners, undefined);
    }

    public onLogin(cb: (signedInUser: SignedInUser) => void): () => void {
        const id = randomUUID();
        this.loginListeners[id] = cb;
        // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
        return () => delete this.loginListeners[id];
    }

    public onLogout(cb: () => void): () => void {
        const id = randomUUID();
        this.logoutListeners[id] = cb;
        // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
        return () => delete this.logoutListeners[id];
    }

    public onChange(cb: () => void): () => void {
        const id = randomUUID();
        this.changeListeners[id] = cb;
        // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
        return () => delete this.changeListeners[id];
    }

    public impersonate(user: UserDetails | null): SignedInUser {
        console.log(`👮 Impersonating ${user?.firstName} ${user?.lastName}`);
        this.impersonating = user;
        this.notifyListeners(this.changeListeners, undefined);
        const impersonatedUser = this.getSignedInUser();
        if (!impersonatedUser) {
            throw new Error('Failed to impersonate user');
        }
        return impersonatedUser;
    }

    private notifyListeners<T>(listeners: Record<string, Callback<T>>, param: T): void {
        Object.values(listeners).forEach((cb: Callback<T>) => cb(param));
    }

    private storeSignedInUser(signedInUser: SignedInUser): void {
        localStorage.setItem('user', JSON.stringify(signedInUser));
    }

    private loadStoredSignedInUser(): SignedInUser | undefined {
        const json = localStorage.getItem('user');
        if (!json) {
            return undefined;
        }
        try {
            return JSON.parse(json);
        } catch (e) {
            console.error(e);
            return undefined;
        }
    }
}
