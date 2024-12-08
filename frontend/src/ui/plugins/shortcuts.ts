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

function left(): void {
    window.dispatchEvent(new CustomEvent('left'));
    const button = document.querySelector('button[name="previous"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function right(): void {
    window.dispatchEvent(new CustomEvent('right'));
    const button = document.querySelector('button[name="next"]');
    if (button) {
        (button as HTMLButtonElement).click();
    }
}

function down(): void {
    window.dispatchEvent(new CustomEvent('down'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
        routerView.scroll({
            top: routerView.scrollTop + 200,
            behavior: 'smooth',
        });
    }
}

function up(): void {
    window.dispatchEvent(new CustomEvent('up'));
    const routerView = document.getElementById('router-view');
    if (routerView) {
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
        // console.log(evt.code);
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
    });
}

registerKeyboardShortcuts();
