import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';

export function formatDateRange(from?: Date, to?: Date, short?: boolean): string {
    const i18n = useI18n();
    const formatFullDate = short ? DateTimeFormat.DD_MM_YYYY : DateTimeFormat.DDD_DD_MM_YYYY;
    const formatWithoutYear = short ? DateTimeFormat.DD_MM : DateTimeFormat.DDD_DD_MM;
    if (!from || !to) {
        if (!from && to) return i18n.d(to, formatFullDate);
        if (from && !to) return i18n.d(from, formatFullDate);
        return '';
    }

    if (from.getFullYear() !== to.getFullYear()) {
        return `${i18n.d(from, formatFullDate)} - ${i18n.d(to, formatFullDate)}`;
    }
    if (from.getMonth() === to.getMonth() && from.getDate() === to.getDate()) {
        return `${i18n.d(from, formatFullDate)}`;
    }
    return `${i18n.d(from, formatWithoutYear)} - ${i18n.d(to, formatFullDate)}`;
}

export function formatTimeRange(from?: Date, to?: Date): string {
    const i18n = useI18n();
    const fromDateFull = from ? i18n.d(from, DateTimeFormat.DD_MM_YYYY) : '';
    const fromDateShort = from ? i18n.d(from, DateTimeFormat.DD_MM) : '';
    const fromTime = from ? i18n.d(from, DateTimeFormat.hh_mm) : '';
    const toDateFull = to ? i18n.d(to, DateTimeFormat.DD_MM_YYYY) : '';
    const toDateShort = to ? i18n.d(to, DateTimeFormat.DD_MM) : '';
    const toTime = to ? i18n.d(to, DateTimeFormat.hh_mm) : '';

    if (from && !to) {
        return `${fromDateFull} ${fromTime}`;
    }
    if (!from && to) {
        return `${toDateFull} ${toTime}`;
    }
    if (fromDateFull === toDateFull) {
        return `${fromDateFull} ${fromTime} - ${toTime}`;
    }
    return `${fromDateShort} ${fromTime} - ${toDateShort} ${toTime}`;
}
