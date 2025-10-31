import { config } from '@/config';
import { initAdapters } from '@/initAdapters';
import { initApplicationServices } from '@/initApplicationServices';
import { initDomainServices } from '@/initDomainServices';
import { initUseCases } from '@/initUseCases';
import { setupVue } from '@/ui';

async function init(): Promise<void> {
    const adapters = initAdapters();

    const serverConfig = await adapters.settingsRepository.readConfig();
    config.menuTitle = serverConfig.menuTitle || 'Reiseplaner';
    config.tabTitle = serverConfig.tabTitle || 'Reiseplaner';
    config.supportEmail = serverConfig.supportEmail || 'support@example.de';
    config.technicalSupportEmail = serverConfig.technicalSupportEmail || 'support@example.de';

    const domainServices = initDomainServices();
    const applicationServices = initApplicationServices({ adapters });
    const useCases = initUseCases({ config, adapters, domainServices, applicationServices });

    await useCases.usersUseCase.applyUserSettings();

    setupVue({ config, domainServices, applicationServices, useCases });
}
init();
