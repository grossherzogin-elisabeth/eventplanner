import { ref } from 'vue';
import type { Ref } from 'vue';
import { useConfigService } from '@/application';
import type { Config } from '@/application';

export interface UseConfig {
    config: Ref<Config>;
}

export function useConfig(): UseConfig {
    const configService = useConfigService();
    const config = ref(configService.getConfig());

    configService.initialization().then(() => {
        config.value = configService.getConfig();
    });

    return { config };
}
