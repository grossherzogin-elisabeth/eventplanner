import { builtinEnvironments, type Environment } from 'vitest/environments';

const happyDOMEnv = builtinEnvironments['happy-dom'];

export default {
    ...happyDOMEnv,
    async setupVM(options) {
        const res = await happyDOMEnv.setupVM!(options);
        const win = res.getVmContext();
        win.BroadcastChannel = BroadcastChannel;
        return res;
    },
} as Environment;
