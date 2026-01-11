import { getCsrfToken } from '@/adapter/util/Csrf';
import type { QualificationRepository } from '@/application';
import type { Qualification, QualificationKey } from '@/domain';

interface CreateQualificationRequest {
    key: string;
    name: string;
    icon: string;
    description: string;
    expires: boolean;
    grantsPositions?: string[];
}

interface UpdateQualificationRequest {
    name: string;
    icon: string;
    description: string;
    expires: boolean;
    grantsPositions?: string[];
}

export interface QualificationRepresentation {
    key: string;
    name: string;
    icon: string;
    description: string;
    expires: boolean;
    grantsPositions?: string[];
}

export class QualificationRestRepository implements QualificationRepository {
    private static mapToDomain(representation: QualificationRepresentation): Qualification {
        return {
            key: representation.key,
            name: representation.name,
            icon: representation.icon,
            description: representation.description,
            expires: representation.expires,
            grantsPositions: representation.grantsPositions || [],
        };
    }

    public async findAll(): Promise<Qualification[]> {
        const response = await fetch('/api/v1/qualifications', { credentials: 'include' });
        if (!response.ok) {
            throw response;
        }
        const qualifications = (await response.clone().json()) as QualificationRepresentation[];
        return qualifications.map(QualificationRestRepository.mapToDomain);
    }

    public async create(qualification: Qualification): Promise<Qualification> {
        const requestBody: CreateQualificationRequest = {
            key: qualification.key,
            name: qualification.name,
            icon: qualification.icon,
            description: qualification.description,
            expires: qualification.expires,
            grantsPositions: qualification.grantsPositions,
        };
        const response = await fetch('/api/v1/qualifications', {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation = (await response.clone().json()) as QualificationRepresentation;
        return QualificationRestRepository.mapToDomain(representation);
    }

    public async update(qualificationKey: QualificationKey, qualification: Qualification): Promise<Qualification> {
        const requestBody: UpdateQualificationRequest = {
            name: qualification.name,
            icon: qualification.icon,
            description: qualification.description,
            expires: qualification.expires,
            grantsPositions: qualification.grantsPositions,
        };
        const response = await fetch(`/api/v1/qualifications/${qualificationKey}`, {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation = (await response.clone().json()) as QualificationRepresentation;
        return QualificationRestRepository.mapToDomain(representation);
    }

    public async deleteByKey(qualificationKey: QualificationKey): Promise<void> {
        const response = await fetch(`/api/v1/qualifications/${qualificationKey}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
    }
}
