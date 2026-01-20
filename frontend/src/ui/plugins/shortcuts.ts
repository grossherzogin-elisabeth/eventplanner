function save(evt: KeyboardEvent): void {
    if (evt.defaultPrevented) return;
    evt.preventDefault();
    globalThis.dispatchEvent(new CustomEvent('save'));
    const button = document.querySelector('button[name="save"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function cancel(evt: KeyboardEvent): void {
    evt.preventDefault();
    globalThis.dispatchEvent(new CustomEvent('cancel'));
}

function left(): void {
    globalThis.dispatchEvent(new CustomEvent('left'));
    const button = document.querySelector('button[name="previous"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function right(): void {
    globalThis.dispatchEvent(new CustomEvent('right'));
    const button = document.querySelector('button[name="next"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function down(): void {
    globalThis.dispatchEvent(new CustomEvent('down'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
        routerView.scroll({
            top: routerView.scrollTop + 200,
            behavior: 'smooth',
        });
    }
}

function up(): void {
    globalThis.dispatchEvent(new CustomEvent('up'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
        routerView.scroll({
            top: routerView.scrollTop - 200,
            behavior: 'smooth',
        });
    }
}

function focusSearch(evt: KeyboardEvent): void {
    if (evt.defaultPrevented) return;
    const input = document.querySelector('input[name="search"]');
    if (input) {
        evt.preventDefault();
        (input as HTMLInputElement).focus();
    }
}

let handler: ((evt: KeyboardEvent) => void) | null = null;

function registerKeyboardShortcuts(): void {
    if (handler) return; // Prevent multiple registrations
    handler = (evt: KeyboardEvent): void => {
        if (evt.metaKey || evt.ctrlKey) {
            switch (evt.code) {
                case 'KeyF':
                    focusSearch(evt);
                    break;
                case 'KeyS':
                    save(evt);
                    break;
                default:
            }
        }
        switch (evt.code) {
            case 'Escape':
                cancel(evt);
                break;
            case 'ArrowLeft':
                left();
                break;
            case 'ArrowRight':
                right();
                break;
            case 'ArrowDown':
                down();
                break;
            case 'ArrowUp':
                up();
                break;
        }
    };
    globalThis.addEventListener('keydown', handler);
}

function unregisterKeyboardShortcuts(): void {
    if (handler) {
        globalThis.removeEventListener('keydown', handler);
        handler = null;
    }
}

export { registerKeyboardShortcuts, unregisterKeyboardShortcuts };
