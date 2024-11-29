function save(evt: KeyboardEvent): void {
    evt.preventDefault();
    window.dispatchEvent(new CustomEvent('save'));
    const button = document.querySelector('button[name="save"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function cancel(evt: KeyboardEvent): void {
    evt.preventDefault();
    window.dispatchEvent(new CustomEvent('cancel'));
}

function focusSearch(evt: KeyboardEvent): void {
    const input = document.querySelector('input[name="search"]');
    if (input) {
        evt.preventDefault();
        (input as HTMLInputElement).focus();
    }
}

function registerKeyboardShortcuts(): void {
    window.addEventListener('keydown', (evt) => {
        if (evt.metaKey) {
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
        if (evt.code == 'Escape') {
            cancel(evt);
        }
    });
}

registerKeyboardShortcuts();
