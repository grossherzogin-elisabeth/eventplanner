export enum DateTimeFormat {
    DD_MM_YYYY = 'DD.MM.YYYY',
    DD_MM = 'DD.MM.',
    DDD = 'DDD',
    DDD_DD_MM = 'DDD DD.MM.',
    DDD_DD_MM_YYYY = 'DDD DD.MM.YYYY',
    MMM = 'MMM',
    MMMM_YYYY = 'MMMM YYYY',
    hh_mm = 'hh:mm',
    DDD_DD_MM_hh_mm = 'DDD DD.MM. hh:mm',
}

export function formatDate(date?: Date | string | number, delimiter: string = '.'): string {
    if (!date) {
        return '';
    }
    const d = new Date(date);
    const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate().toString();
    const month = d.getMonth() < 9 ? `0${d.getMonth() + 1}` : (d.getMonth() + 1).toString();
    const year = d.getFullYear();
    return `${day}${delimiter}${month}${delimiter}${year}`;
}
