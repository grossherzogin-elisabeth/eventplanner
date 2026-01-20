import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { registerKeyboardShortcuts, unregisterKeyboardShortcuts } from '@/ui/plugins/shortcuts.ts';

function dispatchKey(code: string, opts: Partial<KeyboardEventInit> = {}): KeyboardEvent {
    const evt = new KeyboardEvent('keydown', { code, ...opts, bubbles: true, cancelable: true });
    document.dispatchEvent(evt);
    return evt;
}

beforeEach(() => {
    document.body.innerHTML = '';
    vi.resetModules();
    registerKeyboardShortcuts();
});

afterEach(() => {
    vi.restoreAllMocks();
    unregisterKeyboardShortcuts();
});

describe('keyboard shortcuts', () => {
    it('Ctrl+S triggers save event and clicks save button', async () => {
        const btn = document.createElement('button');
        btn.setAttribute('name', 'save');
        const clickSpy = vi.spyOn(btn, 'click');
        document.body.appendChild(btn);

        let saveEventFired = false;
        globalThis.addEventListener('save', () => (saveEventFired = true), { once: true });

        const preventSpy = vi.fn();
        const evt = new KeyboardEvent('keydown', { code: 'KeyS', ctrlKey: true, bubbles: true, cancelable: true });
        Object.defineProperty(evt, 'preventDefault', { value: preventSpy });
        document.dispatchEvent(evt);

        expect(saveEventFired).toBe(true);
        expect(clickSpy).toHaveBeenCalledTimes(1);
        expect(preventSpy).toHaveBeenCalledTimes(1);
    });

    it('Meta+S (Cmd+S) also triggers save', async () => {
        const btn = document.createElement('button');
        btn.setAttribute('name', 'save');
        const clickSpy = vi.spyOn(btn, 'click');
        document.body.appendChild(btn);

        let saveEventFired = false;
        globalThis.addEventListener('save', () => (saveEventFired = true), { once: true });

        dispatchKey('KeyS', { metaKey: true });

        expect(saveEventFired).toBe(true);
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });

    it('Escape triggers cancel event', async () => {
        let cancelEventFired = false;
        globalThis.addEventListener('cancel', () => (cancelEventFired = true), { once: true });

        const evt = dispatchKey('Escape', {});
        const preventSpy = vi.fn();
        Object.defineProperty(evt, 'preventDefault', { value: preventSpy });
        document.dispatchEvent(evt);

        expect(cancelEventFired).toBe(true);
        expect(preventSpy).toHaveBeenCalledTimes(1);
    });

    it('ArrowLeft triggers left event and clicks previous button', async () => {
        const btn = document.createElement('button');
        btn.setAttribute('name', 'previous');
        const clickSpy = vi.spyOn(btn, 'click');
        document.body.appendChild(btn);

        let leftEventFired = false;
        globalThis.addEventListener('left', () => (leftEventFired = true), { once: true });

        dispatchKey('ArrowLeft');

        expect(leftEventFired).toBe(true);
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });

    it('ArrowRight triggers right event and clicks next button', async () => {
        const btn = document.createElement('button');
        btn.setAttribute('name', 'next');
        const clickSpy = vi.spyOn(btn, 'click');
        document.body.appendChild(btn);

        let rightEventFired = false;
        globalThis.addEventListener('right', () => (rightEventFired = true), { once: true });

        dispatchKey('ArrowRight');

        expect(rightEventFired).toBe(true);
        expect(clickSpy).toHaveBeenCalledTimes(1);
    });

    it('ArrowDown triggers down event and scrolls router-view securely', async () => {
        const view = document.createElement('div');
        view.id = 'router-view';
        Object.defineProperty(view, 'scrollTop', { value: 100, writable: true });
        const scrollSpy = vi.fn();
        (view as HTMLElement).scroll = scrollSpy;
        document.body.appendChild(view);

        let downEventFired = false;
        globalThis.addEventListener('down', () => (downEventFired = true), { once: true });

        dispatchKey('ArrowDown');

        expect(downEventFired).toBe(true);
        expect(scrollSpy).toHaveBeenCalledWith({ top: 300, behavior: 'smooth' });
    });

    it('ArrowUp triggers up event and scrolls router-view securely', async () => {
        const view = document.createElement('div');
        view.id = 'router-view';
        Object.defineProperty(view, 'scrollTop', { value: 300, writable: true });
        const scrollSpy = vi.fn();
        (view as HTMLElement).scroll = scrollSpy;
        document.body.appendChild(view);

        let upEventFired = false;
        globalThis.addEventListener('up', () => (upEventFired = true), { once: true });

        dispatchKey('ArrowUp');

        expect(upEventFired).toBe(true);
        expect(scrollSpy).toHaveBeenCalledWith({ top: 100, behavior: 'smooth' });
    });

    it('Ctrl+F focuses search input securely', async () => {
        const input = document.createElement('input');
        input.setAttribute('name', 'search');
        const focusSpy = vi.spyOn(input, 'focus');
        document.body.appendChild(input);

        const preventSpy = vi.fn();
        const evt = new KeyboardEvent('keydown', { code: 'KeyF', ctrlKey: true, bubbles: true, cancelable: true });
        Object.defineProperty(evt, 'preventDefault', { value: preventSpy });
        document.dispatchEvent(evt);

        expect(focusSpy).toHaveBeenCalledTimes(1);
        expect(document.activeElement).toBe(input);
        expect(preventSpy).toHaveBeenCalledTimes(1);
    });
});
