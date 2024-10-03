import { ArrayUtils } from '@/common';
import type { Event, Position, Registration, ResolvedRegistration, ResolvedSlot, User } from '@/domain';

export class RegistrationService {
    public resolveRegistrationsWithAssignedSlots(
        event: Event,
        users: User[],
        positions: Map<string, Position>
    ): ResolvedSlot[] {
        return event.slots
            .map((slot) => {
                const registration = event.registrations.find((it) => it.key === slot.assignedRegistrationKey);
                if (registration) {
                    const userName = this.resolveRegistrationUserName(registration, users);
                    const position = positions.get(registration.positionKey) || this.unknownPosition();
                    return {
                        ...slot,
                        userName: userName,
                        userKey: registration.userKey,
                        registration: registration,
                        position: position,
                        positionName: slot.positionName || position.name,
                        confirmed: false,
                    };
                }
                const position = positions.get(slot.positionKeys[0]) || this.unknownPosition();
                return {
                    ...slot,
                    userName: undefined,
                    userKey: undefined,
                    registration: registration,
                    position: position,
                    positionName: slot.positionName || position.name,
                    confirmed: false,
                };
            })
            .sort(
                (a, b) =>
                    b.position.prio - a.position.prio ||
                    a.order - b.order ||
                    a.userName?.localeCompare(b.userName || '') ||
                    0
            );
    }

    public resolveRegistrationsOnWaitingList(
        event: Event,
        users: User[],
        positions: Map<string, Position>
    ): ResolvedRegistration[] {
        const assignedRegistrationKeys = event.slots
            .map((it) => it.assignedRegistrationKey)
            .filter(ArrayUtils.filterUndefined);
        return event.registrations
            .filter((registration) => !assignedRegistrationKeys.includes(registration.key))
            .map(
                (registration) =>
                    ({
                        ...registration,
                        name: this.resolveRegistrationUserName(registration, users),
                        position: positions.get(registration.positionKey) || this.unknownPosition(),
                        user: this.resolveRegistrationUser(registration, users),
                    }) as ResolvedRegistration
            )
            .filter(ArrayUtils.filterUndefined)
            .sort((a, b) => b.position.prio - a.position.prio || a.name.localeCompare(b.name));
    }

    private resolveRegistrationUser(registration: Registration, users: User[]): User | undefined {
        if (!registration.userKey) {
            return undefined;
        }
        return users.find((user) => user.key === registration.userKey);
    }

    private resolveRegistrationUserName(registration: Registration, users: User[]): string {
        const user = this.resolveRegistrationUser(registration, users);
        if (!user) {
            return registration.name || '';
        }
        return `${user.nickName || user.firstName} ${user.lastName}`.trim();
    }

    private unknownPosition(): Position {
        return { key: '', name: '?', prio: 0, color: '' };
    }
}
