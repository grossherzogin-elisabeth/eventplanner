import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';

export function formatDateRange(from?: Date, to?: Date): string {
    const i18n = useI18n();
    if (!from || !to) {
        if (!from && to) return i18n.d(to, DateTimeFormat.DDD_DD_MM_YYYY);
        if (from && !to) return i18n.d(from, DateTimeFormat.DDD_DD_MM_YYYY);
        return '';
    }

    if (from.getFullYear() !== to.getFullYear()) {
        return `${i18n.d(from, DateTimeFormat.DDD_DD_MM_YYYY)} - ${i18n.d(to, DateTimeFormat.DDD_DD_MM_YYYY)}`;
    }
    if (from.getMonth() === to.getMonth() && from.getDate() === to.getDate()) {
        return `${i18n.d(from, DateTimeFormat.DDD_DD_MM_YYYY)}`;
    }
    return `${i18n.d(from, DateTimeFormat.DDD_DD_MM)} - ${i18n.d(to, DateTimeFormat.DDD_DD_MM_YYYY)}`;
}
