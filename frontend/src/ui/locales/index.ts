import deGeneric from './de/generic.yml';
import deNavigation from './de/navigation.yml';
import deComponents from './de/components.yml';
import deDomain from './de/domain.yml';
import deViews from './de/views.yml';

const messagesDe = {
  ...deGeneric,
  ...deNavigation,
  ...deComponents,
  ...deDomain,
  ...deViews,
};

export default {
  de: messagesDe,
};

