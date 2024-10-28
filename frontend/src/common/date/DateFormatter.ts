export enum DateTimeFormat {
    DD_MM_YYYY = 'DD.MM.YYYY',
    DD_MM = 'DD.MM.',
    DDD = 'DDD',
    DDD_DD_MM = 'DDD DD.MM.',
    DDD_DD_MM_YYYY = 'DDD DD.MM.YYYY',
    MMM = 'MMM',
    MMMM_YYYY = 'MMMM YYYY',
    hh_mm = 'hh:mm',
}

export function formatDate(date?: Date | string | number): string {
    if (!date) {
        return '';
    }
    const d = new Date(date);
    const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate().toString();
    const month = d.getMonth() < 9 ? `0${d.getMonth() + 1}` : (d.getMonth() + 1).toString();
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
}

export function formatIcsDate(date: Date): string {
    const year = String(date.getFullYear());
    let month = String(date.getMonth() + 1);
    if (month.length === 1) month = `0${month}`;
    let day = String(date.getDate());
    if (day.length === 1) day = `0${day}`;
    let hour = String(date.getHours());
    if (hour.length === 1) hour = `0${hour}`;
    let minute = String(date.getMinutes());
    if (minute.length === 1) minute = `0${minute}`;
    return `${year}${month}${day}T${hour}${minute}00Z`;
}
