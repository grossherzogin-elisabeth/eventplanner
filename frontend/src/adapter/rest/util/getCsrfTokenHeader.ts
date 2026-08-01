export function getCsrfTokenHeader(): Record<string, string> {
    const token = getCookieValue('XSRF-TOKEN');
    if (token) {
        return { 'X-XSRF-TOKEN': token };
    }
    return {};
}

function getCookieValue(name: string): string {
    return document.cookie.match('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)')?.pop() || '';
}
