import deComponents from './de/components.yml';
import deGeneric from './de/generic.yml';
import deNavigation from './de/navigation.yml';

import deHome from './de/views/home.yml';
import deSettings from './de/views/settings.yml';
import deAccount from './de/views/account.yml';
import deBasedata from './de/views/basedata.yml';
import deEvents from './de/views/events.yml';
import deUsers from './de/views/users.yml';

const messagesDe = {
    ...deGeneric,
    ...deNavigation,
    ...deComponents,
    views: {
        ...deHome,
        ...deSettings,
        ...deAccount,
        ...deBasedata,
        ...deEvents,
        ...deUsers,
    },
};

export default {
    de: messagesDe,
};
