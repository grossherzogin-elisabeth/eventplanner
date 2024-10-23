import type { Position, ValidationHint } from '@/domain';

export class PositionService {
    public validate(position: Position): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (position.name.trim().length === 0) {
            errors.name = errors.name || [];
            errors.name.push({
                key: 'Bitte gib einen Anzeigenamen für die Position an',
                params: {},
            });
        }
        if (position.color.trim().length === 0) {
            errors.description = errors.description || [];
            errors.description.push({
                key: 'Bitte gib eine Anzeigefarbe für die Position an',
                params: {},
            });
        }
        return errors;
    }
}
