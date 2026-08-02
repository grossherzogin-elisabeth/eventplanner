export function getCsrfTokenHeader(): Record<string, string> {
    const token = getCookieValue('XSRF-TOKEN');
    if (token) {
        return { 'X-XSRF-TOKEN': token };
    }
    return {};
}

function getCookieValue(name: string): string | undefined {
    const regex = new RegExp('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)');
    return regex.exec(decodeURIComponent(document.cookie))?.pop() ?? undefined;
}
