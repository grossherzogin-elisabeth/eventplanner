import { ref } from 'vue';
import type { Ref } from 'vue';
import { useEventExportUseCase } from '@/application';
import type { Event } from '@/domain';

export interface UseEventTemplates {
    templates: Ref<string[]>;
    loading: Promise<void>;
    exportEvent(event: Event, templateName: string): Promise<void>;
}

export function useEventExports(): UseEventTemplates {
    const eventExportUseCase = useEventExportUseCase();
    const templates = ref<string[]>([]);

    async function exportEvent(event: Event, templateName: string): Promise<void> {
        await eventExportUseCase.exportEvent(event, templateName);
    }

    async function update(): Promise<void> {
        templates.value = await eventExportUseCase.getExportTemplates();
    }
    const loading = update();

    return { templates, loading, exportEvent };
}
