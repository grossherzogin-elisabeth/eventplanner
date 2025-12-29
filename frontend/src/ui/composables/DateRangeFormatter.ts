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
