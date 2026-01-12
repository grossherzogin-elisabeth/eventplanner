import indexeddb from 'fake-indexeddb';
import { type Environment, builtinEnvironments } from 'vitest/environments';

class ResizeObserver {
    public observe(): void {
        // do nothing
    }
    public unobserve(): void {
        // do nothing
    }
    public disconnect(): void {
        // do nothing
    }
}

const environment = builtinEnvironments['happy-dom'];

export default {
    ...environment,
    async setupVM(options) {
        if (!environment.setupVM) {
            throw Error('setupVM is missing');
        }
        const vm = await environment.setupVM(options);
        const w = vm.getVmContext();
        w.BroadcastChannel = BroadcastChannel;
        w.ResizeObserver = ResizeObserver;
        w.indexedDB = indexeddb;

        // EventTarget is the parent of VisualViewport. Mocking visualViewport with
        // an EventTarget instance omits some properties defined in the subtype
        // (VisualViewport), but these are probably not needed for testing purposes
        // https://developer.mozilla.org/en-US/docs/Web/API/VisualViewport
        w.visualViewport = new EventTarget();
        w.alert = (s: string): void => console.log(s);
        return vm;
    },
} as Environment;
