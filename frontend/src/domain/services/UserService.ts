import { DateUtils } from '@/common';
import type { Qualification, QualificationKey, User, UserDetails } from '@/domain';
import type { ResolvedUserQualification } from '@/domain/aggregates/ResolvedUserQualification';

export class UserService {
    public doesUserMatchFilter(user: User, filter: string): boolean {
        const filterLc = filter.toLowerCase();
        const fullname = `${user.firstName} ${user.lastName}`.toLowerCase().trim();
        if (fullname.includes(filterLc)) {
            return true;
        }
        const nickname = `${user.nickName} ${user.lastName}`.toLowerCase().trim();
        if (nickname.includes(filterLc)) {
            return true;
        }
        if (user.email?.toLowerCase().includes(filterLc)) {
            return true;
        }
        return false;
    }

    public resolveQualifications(
        user: UserDetails,
        qualifications: Map<QualificationKey, Qualification>
    ): ResolvedUserQualification[] {
        const now = new Date();
        const expiresSoonDate = DateUtils.add(now, { months: 3 });

        return user.qualifications.map((it) => {
            const qualification = qualifications.get(it.qualificationKey);
            const isExpired = it.expiresAt ? it.expiresAt.getTime() < now.getTime() : false;
            const willExpireSoon = it.expiresAt ? it.expiresAt.getTime() < expiresSoonDate.getTime() : false;

            if (qualification) {
                return {
                    ...qualification,
                    expiresAt: it.expiresAt,
                    isExpired,
                    willExpireSoon,
                };
            }

            return {
                key: it.qualificationKey,
                name: it.qualificationKey,
                icon: 'fa-circle-question',
                description: 'Unbekannte Qualification',
                expires: it.expiresAt !== undefined,
                expiresAt: it.expiresAt,
                isExpired,
                willExpireSoon,
            };
        });
    }

    public getExpiredQualifications(user?: User, at: Date = new Date()): QualificationKey[] {
        if (!user?.qualifications) {
            return [];
        }
        const referenceTime = at.getTime();
        return user.qualifications
            .filter((it) => it.expiresAt !== undefined && it.expiresAt.getTime() <= referenceTime)
            .map((it) => it.qualificationKey);
    }

    public getSoonExpiringQualifications(user?: User, at: Date = new Date()): QualificationKey[] {
        const referenceTime = DateUtils.add(at, { months: 3 });
        return this.getExpiredQualifications(user, referenceTime);
    }
}
