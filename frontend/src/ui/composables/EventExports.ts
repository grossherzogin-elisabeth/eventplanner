import { ref } from 'vue';
import type { Ref } from 'vue';
import { useEventExportUseCase } from '@/application';
import type { Event } from '@/domain';

export interface UseEventExports {
    templates: Ref<string[]>;
    loading: Promise<void>;
    exportEvents(year: number): Promise<void>;
    exportEvent(event: Event, templateName: string): Promise<void>;
}

export function useEventExports(): UseEventExports {
    const eventExportUseCase = useEventExportUseCase();
    const templates = ref<string[]>([]);

    async function exportEvent(event: Event, templateName: string): Promise<void> {
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
