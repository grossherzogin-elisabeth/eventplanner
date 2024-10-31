/**
 * Prevent document scrolling, e.g. when a dialog gets opened.
 */
export function disableScrolling(): void {
    const scrollY = window.scrollY;
    const scrollbarWidth = window.innerWidth - (visualViewport?.width || 0);
    document.documentElement.setAttribute('scrollTop', scrollY.toString());
    document.documentElement.style.setProperty('overflow', 'hidden');
    document.documentElement.style.setProperty('position', 'fixed');
    document.documentElement.style.setProperty('width', '100vw');
    document.documentElement.style.setProperty('height', '100vh');
    document.documentElement.style.setProperty('margin-top', `-${scrollY}px`);
    // padding right to preserve space for the scrollbar
    document.documentElement.style.setProperty('padding-right', `${scrollbarWidth}px`);
}

/**
 * Undo the disableScrolling function.
 */
export function enableScrolling(): void {
    document.documentElement.style.removeProperty('overflow');
    document.documentElement.style.removeProperty('position');
    document.documentElement.style.removeProperty('width');
    document.documentElement.style.removeProperty('height');
    document.documentElement.style.removeProperty('margin-top');
    document.documentElement.style.removeProperty('padding-right');
    const scrollY = parseInt(document.documentElement.getAttribute('scrollTop') || '0', 10);
    window.scrollTo(0, scrollY);
}

export function isTouchDevice() {
    return 'ontouchstart' in window || navigator.maxTouchPoints > 0 || navigator.msMaxTouchPoints > 0;
}
