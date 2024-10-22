import type { Qualification, ValidationHint } from '@/domain';

export class QualificationService {
    public validate(qualification: Qualification): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (qualification.name.trim().length === 0) {
            errors.name = errors.name || [];
            errors.name.push({
                key: 'Bitte gib einen Anzeigenamen für die Qualifikation an',
                params: {},
            });
        }
        if (qualification.description.trim().length === 0) {
            errors.description = errors.description || [];
            errors.description.push({
                key: 'Bitte gib eine Beschreibung für die Qualifikation an',
                params: {},
            });
        }
        if (qualification.icon.trim().length === 0) {
            errors.icon = errors.description || [];
            errors.icon.push({
                key: 'Bitte gib ein Icon für die Qualifikation an',
                params: {},
            });
        }
        return errors;
    }
}
