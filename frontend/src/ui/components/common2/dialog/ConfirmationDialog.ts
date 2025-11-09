import type { Dialog } from '@/ui/components/common';

export interface ConfirmationDialogContent {
    title?: string;
    message?: string;
    cancel?: string;
    submit?: string;
    danger?: boolean;
}

export type ConfirmationDialog = Dialog<ConfirmationDialogContent | undefined, boolean | undefined>;
