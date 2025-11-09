export function getCsrfToken(): string {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let c of ca) {
        while (c.startsWith(' ')) {
            c = c.substring(1);
        }
        if (c.startsWith(name)) {
            return c.substring(name.length, c.length);
        }
    }
    return '';
}
