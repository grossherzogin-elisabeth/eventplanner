import de from './de.yml';
import deComponents from './de/components.yml';
import deGeneric from './de/generic.yml';
import deNavigation from './de/navigation.yml';
import deViews from './de/views.yml';

const messagesDe = {
    ...de,
    ...deGeneric,
    ...deNavigation,
    ...deComponents,
    ...deViews,
};

export default {
    de: messagesDe,
};
