import { ref } from 'vue';
import type { Ref } from 'vue';
import { useEventExportUseCase, useNotificationService } from '@/application';
import type { Event } from '@/domain';

export interface UseEventExports {
    templates: Ref<string[]>;
    loading: Promise<void>;
    exportEvents(year: number): Promise<void>;
    exportEvent(event: Event, templateName: string): Promise<void>;
}

export function useEventExports(): UseEventExports {
    const eventExportUseCase = useEventExportUseCase();
    const notifications = useNotificationService();
    const templates = ref<string[]>([]);

    async function exportEvent(event: Event, templateName: string): Promise<void> {
        notifications.info('Dein Export wird erstellt. Bitte habe einen Moment Geduld.');
        await eventExportUseCase.exportEvent(event, templateName);
    }

    async function exportEvents(year: number): Promise<void> {
        await eventExportUseCase.exportEvents(year);
    }

    async function update(): Promise<void> {
        templates.value = await eventExportUseCase.getExportTemplates();
    }
    const loading = update();

    return { templates, loading, exportEvents, exportEvent };
}
