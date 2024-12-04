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

function left(evt: KeyboardEvent): void {
    window.dispatchEvent(new CustomEvent('left'));
    const button = document.querySelector('button[name="previous"]');
    if (button) {
        evt.preventDefault();
        (button as HTMLButtonElement).click();
    }
}

function right(evt: KeyboardEvent): void {
    window.dispatchEvent(new CustomEvent('right'));
    const button = document.querySelector('button[name="next"]');
    if (button) {
        evt.preventDefault();
        (button as HTMLButtonElement).click();
    }
}

function down(evt: KeyboardEvent): void {
    window.dispatchEvent(new CustomEvent('down'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
        evt.preventDefault();
        routerView.scroll({
            top: routerView.scrollTop + 200,
            behavior: 'smooth',
        });
    }
}

function up(evt: KeyboardEvent): void {
    window.dispatchEvent(new CustomEvent('up'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
        evt.preventDefault();
        routerView.scroll({
            top: routerView.scrollTop - 200,
            behavior: 'smooth',
        });
    }
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
        console.log(evt.code);
        switch (evt.code) {
            case 'Escape':
                cancel(evt);
                break;
            case 'ArrowLeft':
                left(evt);
                break;
            case 'ArrowRight':
                right(evt);
                break;
            case 'ArrowDown':
                down(evt);
                break;
            case 'ArrowUp':
                up(evt);
                break;
        }
    });
}

registerKeyboardShortcuts();
