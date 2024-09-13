import type { QualificationRepository } from '@/application';
import type { Qualification } from '@/domain';

interface QualificationRepresentation {
    key: string;
    name: string;
    icon: string;
    description: string;
    expires: boolean;
}

export class QualificationRestRepository implements QualificationRepository {
    public async findAll(): Promise<Qualification[]> {
        const response = await fetch('/api/v1/qualifications', { credentials: 'include' });
        if (response.ok) {
            const qualifications = (await response.clone().json()) as QualificationRepresentation[];
            return qualifications.map((it) => ({
                key: it.key,
                name: it.name,
                icon: it.icon,
                description: it.description,
                expires: it.expires,
            }));
        } else {
            throw response;
        }
    }
}
