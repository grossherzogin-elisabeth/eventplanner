declare const value: {
    components: {
        'create-registration-for-user-dialog': {
            'note-adding-to-waiting-list': string;
        };
        'create-user-dialog': {
            'note-edits': string;
        };
        'error-dialog': {
            'default-text': string;
            'default-title': string;
            'details': string;
            'retry': string;
        };
        'event-cancel-dialog': {
            message: string;
            submit: string;
            title: string;
        };
        'event-create-dialog': {
            'description-placeholder': string;
            'name-placeholder': string;
        };
        'event-details-card': {
            assigned: string;
            registrations: string;
            title: string;
            waitinglist: string;
        };
        'event-locations-card': {
            'address-link': string;
            'learn-more': string;
            'placeholder': string;
        };
        'event-participants-card': {
            'no-registrations': string;
            'no-waitinglist': string;
            'placeholder': string;
            'placeholder-title': string;
            'placeholder-title-f': string;
            'placeholder-title-m': string;
            'placeholder-waiting-list-hint': string;
        };
        'event-registration-details-card': {
            title: string;
        };
        'event-state-banner': {
            'assigned': string;
            'canceled': string;
            'confirmed': string;
            'draft': string;
            'missing-crew': string;
            'planning': string;
            'waiting-list': string;
        };
        'menu': {
            'account': string;
            'calendar': string;
            'events': string;
            'events-admin': string;
            'home': string;
            'impersonate': string;
            'no-account': {
                header: string;
                login: string;
                message: string;
                register: string;
            };
            'settings': string;
            'sign-out': string;
            'start': string;
            'stop-impersonate': string;
            'users': string;
            'users-admin': string;
        };
        'registration-arrival-card': {
            description: string;
            no: string;
            note: string;
            yes: string;
        };
        'registration-note-card': {
            description: string;
            none: string;
        };
        'registration-overnight-stay-card': {
            description: string;
            note: string;
        };
        'registration-position-card': {
            description: string;
            label: string;
        };
        'user-emergency-form': {
            'note-privacy': string;
            'short-title': string;
            'title': string;
        };
        'user-other-form': {
            comment: string;
            diet: string;
            other: string;
        };
    };
    domain: {
        'address': {
            'address-line-1': string;
            'address-line-2': string;
            'country': string;
            'town': string;
            'zipcode': string;
        };
        'emergency-contact': {
            name: string;
            phone: string;
        };
        'event': {
            'actions': {
                'cancel': string;
                'contact-crew': string;
                'create': string;
                'create-calendar-entry': string;
                'delete': string;
                'edit': string;
                'export-to-template': string;
                'leave-waiting-list': string;
                'publish-crew': string;
                'reset-crew': string;
                'sign-up': string;
                'start-crew-signup': string;
                'view': string;
            };
            'category': string;
            'crew': string;
            'crew-count': string;
            'description': string;
            'end-date': string;
            'end-time': string;
            'location': string;
            'name': string;
            'no-slot-for-position-error': {
                message: string;
                retry: string;
                title: string;
            };
            'registration-count': string;
            'registrations': string;
            'signup-type': string;
            'start': string;
            'start-date': string;
            'start-time': string;
            'status': string;
            'template': string;
            'time': string;
            'type': string;
            'waiting-list': string;
        };
        'event-signup-type': {
            assignment: string;
            open: string;
        };
        'event-slot': {
            'actions': {
                create: string;
                delete: string;
                duplicate: string;
                edit: string;
            };
            'alternative-positions': string;
            'criticality': string;
            'display-name': string;
            'empty': string;
            'important': string;
            'optional': string;
            'position': string;
            'required': string;
        };
        'event-state': {
            'canceled': string;
            'crew-wanted': string;
            'draft': string;
            'full': string;
            'open-for-signup': string;
            'open-slots': string;
            'planned': string;
        };
        'event-type': {
            'multi-day-event': string;
            'other': string;
            'single-day-event': string;
            'training-event': string;
            'weekend-event': string;
            'work-event': string;
        };
        'location': {
            'actions': {
                'create': string;
                'delete': string;
                'delete-eta': string;
                'delete-etd': string;
                'edit': string;
            };
            'address': string;
            'address-link': string;
            'arrival': string;
            'departure': string;
            'eda': string;
            'edd': string;
            'eta': string;
            'etd': string;
            'icon': string;
            'information': string;
            'information-link': string;
            'name': string;
        };
        'position': {
            'actions': {
                create: string;
                edit: string;
            };
            'color': string;
            'imo-list-rank': string;
            'key': string;
            'name': string;
            'prio': string;
            'validation': {
                'key-must-be-unique': string;
                'name-must-be-unique': string;
            };
        };
        'qualification': {
            'actions': {
                create: string;
                delete: string;
                edit: string;
            };
            'description': string;
            'expires': string;
            'expires-on': string;
            'icon': string;
            'key': string;
            'name': string;
            'no-expiration-date': string;
            'positions': string;
            'validation': {
                'key-must-be-unique': string;
                'name-must-be-unique': string;
            };
            'with-expiration-date': string;
        };
        'registration': {
            'actions': {
                'add-note': string;
                'assign': string;
                'cancel': string;
                'create': string;
                'delete': string;
                'edit': string;
                'leave-waiting-list': string;
                'unassign': string;
            };
            'arrival': string;
            'arrival-on-day-before': string;
            'confirmation-pending': string;
            'confirmed': string;
            'guest': string;
            'name': string;
            'note': string;
            'overnight-stay': string;
            'position': string;
            'unknown-user': string;
            'user': string;
        };
        'role': {
            ROLE_ADMIN: string;
            ROLE_EVENT_LEADER: string;
            ROLE_EVENT_PLANNER: string;
            ROLE_TEAM_MEMBER: string;
            ROLE_TEAM_PLANNER: string;
            ROLE_USER_MANAGER: string;
        };
        'types': {
            event: string;
            location: string;
            position: string;
            qualification: string;
            registration: string;
            user: string;
        };
        'user': {
            'actions': {
                'create': string;
                'impersonate': string;
                'view': string;
                'write-email': string;
            };
            'address': string;
            'auth-key': string;
            'date-and-place-of-birth': string;
            'date-of-birth': string;
            'diet': string;
            'diseases': string;
            'email': string;
            'email-and-phone': string;
            'emergency-contact': string;
            'first-name': string;
            'gender': string;
            'generic-data': string;
            'intolerances': string;
            'last-login-at': string;
            'last-name': string;
            'medication': string;
            'middle-name': string;
            'mobile': string;
            'name': string;
            'nationality': string;
            'nick-name': string;
            'no-login-recorded': string;
            'official-name-hint': string;
            'pass-nr': string;
            'personal-data': string;
            'phone': string;
            'phone-work': string;
            'place-of-birth': string;
            'title': string;
            'verified-at': string;
        };
        'user-qualification': {
            'actions': {
                create: string;
                delete: string;
                edit: string;
            };
            'expired': string;
            'expired-count': string;
            'expires-at': string;
            'expiring-soon': string;
            'expiring-soon-count': string;
            'note': string;
            'valid': string;
        };
    };
    generic: {
        'add': string;
        'apply': string;
        'cancel': string;
        'click-to-show': string;
        'close': string;
        'created-at': string;
        'days': string;
        'delete': string;
        'deselect': string;
        'diet': {
            omnivore: string;
            vegan: string;
            vegetarian: string;
        };
        'discard-changes': string;
        'edit': string;
        'error': string;
        'filter-entries': string;
        'gender': {
            d: string;
            f: string;
            m: string;
        };
        'language': {
            de: string;
            en: string;
        };
        'loading': string;
        'markdown-supported': string;
        'month': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
            '7': string;
            '8': string;
            '9': string;
            '10': string;
            '11': string;
        };
        'month-short': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
            '7': string;
            '8': string;
            '9': string;
            '10': string;
            '11': string;
        };
        'move-down': string;
        'move-up': string;
        'no': string;
        'no-data': string;
        'no-entries': string;
        'no-information': string;
        'no-matches': string;
        'ok': string;
        'optional': string;
        'please-select': string;
        'save': string;
        'select': string;
        'select-all': string;
        'selected-count': string;
        'show-details': string;
        'theme': {
            dark: string;
            light: string;
            system: string;
        };
        'unsaved-changes-dialog': {
            message: string;
            submit: string;
            title: string;
        };
        'updated-at': string;
        'validation': {
            'after': string;
            'before': string;
            'invalid': string;
            'invalid-date': string;
            'invalid-email': string;
            'invalid-name': string;
            'invalid-phone-number': string;
            'max-length': string;
            'min-length': string;
            'must-not-include-lastname': string;
            'required': string;
        };
        'view': string;
        'weekday-short': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
        };
        'yes': string;
    };
    views: {
        'account': {
            'app-settings': {
                'language': string;
                'language-description': string;
                'preferred-position': string;
                'preferred-position-description': string;
                'theme': string;
                'title': string;
            };
            'contact': {
                'address-description': string;
                'email-description': string;
                'phone-description': string;
                'title': string;
            };
            'diet': {
                'diet-description': string;
                'intolerances-description': string;
                'kitchen-info': string;
            };
            'emergency': {
                'diseases-description': string;
                'emergency-contact-description': string;
                'medication-description': string;
                'privacy': string;
                'title': string;
            };
            'personal': {
                'gender-description': string;
                'hint': string;
                'name-description': string;
                'nationality-description': string;
                'nick-name-hint': string;
                'passport-description': string;
                'passport-info': string;
                'passport-link': string;
                'title': string;
            };
            'tab': {
                data: string;
                qualifications: string;
                settings: string;
            };
            'title': string;
        };
        'calendar': {
            'create-event': string;
            'title': string;
        };
        'event-admin-list': {
            'batch-edit': {
                'copy-slots-from': string;
                'copy-slots-warning': string;
                'info': string;
                'not-changed': string;
                'title': string;
            };
            'dialog': {
                'delete': {
                    message: string;
                    submit: string;
                    title: string;
                };
                'open-signup': {
                    cancel: string;
                    message: string;
                    submit: string;
                    title: string;
                };
                'publish-crew': {
                    cancel: string;
                    message: string;
                    submit: string;
                    title: string;
                };
            };
            'export': string;
            'filter': {
                'all-events': string;
                'all-status': string;
                'free-slots': string;
                'search': string;
                'waitinglist': string;
            };
            'state': {
                'free-slots-for': string;
                'missing-crew': string;
            };
            'tab': {
                future: string;
            };
            'tab-title': string;
        };
        'event-confirm-participation': {
            'canceled': {
                info: string;
                title: string;
            };
            'canceled-now': {
                info: string;
                title: string;
            };
            'confirm': string;
            'confirmed': {
                info: string;
                title: string;
            };
            'decline': string;
            'details': {
                title: string;
            };
            'info': {
                deadline: string;
                message: string;
            };
            'invalid-link': {
                info: string;
                title: string;
            };
            'no-registration': {
                info: string;
                title: string;
            };
            'not-found': {
                info: string;
                title: string;
            };
            'title': string;
        };
        'event-details': {
            'leave-crew-dialog': {
                message: string;
                submit: string;
                title: string;
            };
        };
        'event-edit': {
            'edit-registration': {
                'guest-title': string;
                'guest-warning': string;
                'no-qualification': string;
                'qualifications': string;
                'title': string;
            };
            'empty-crew': {
                desc: string;
                title: string;
            };
            'empty-waitinglist': {
                desc: string;
                title: string;
            };
            'filter': {
                'all-positions': string;
                'free-slots': string;
                'pending-confirmation': string;
                'valid-qualifications': string;
            };
            'free': string;
            'info-canceled': string;
            'info-draft': string;
            'info-missing-crew': string;
            'info-signup': string;
            'secure-crew': string;
            'tab': {
                crew: string;
                data: string;
                locations: string;
                registrations: string;
                slots: string;
            };
            'tooltip': {
                'guest-info': string;
                'information': string;
                'positions': string;
                'qualifications': string;
            };
        };
        'event-list': {
            'filter': {
                'all-types': string;
                'assigned': string;
                'free-slots': string;
                'search': string;
                'waitinglist': string;
            };
            'note-no-position': string;
            'state': {
                assigned: string;
                waitinglist: string;
            };
            'tab': {
                future: string;
            };
        };
        'home': {
            'find-next-event': string;
            'loading-events': string;
            'next-month': string;
            'no-upcoming-events-description': string;
            'no-upcoming-events-title': string;
            'tab-title': string;
            'this-month': string;
            'waitinglist': string;
        };
        'settings': {
            filter: {
                expires: string;
                positions: string;
            };
            notifications: {
                'email': string;
                'email-description': string;
                'teams-webhook-not-set-up': string;
                'teams-webhook-set-up': string;
                'teams-webhook-url': string;
                'teams-webhook-url-description': string;
                'title': string;
            };
            positions: {
                'delete-message': string;
                'delete-title': string;
            };
            qualifications: {
                'delete-message': string;
                'delete-title': string;
            };
            tab: {
                email: string;
                general: string;
                notifications: string;
                positions: string;
                qualifications: string;
            };
            title: string;
            ui: {
                'menu-title': string;
                'menu-title-description': string;
                'support-email': string;
                'support-email-description': string;
                'tab-title': string;
                'tab-title-description': string;
                'tech-support-email': string;
                'tech-support-email-description': string;
                'theme-color': string;
                'theme-color-description': string;
                'title': string;
            };
        };
        'unauthorized': {
            message: string;
            title: string;
        };
        'user-details': {
            tab: {
                certificates: string;
                contact: string;
                data: string;
                emergency: string;
                events: string;
                other: string;
                roles: string;
            };
        };
        'user-list': {
            'filter': {
                'active-crew': string;
                'all-events': string;
                'all-positions': string;
                'expired-qualifications': string;
                'not-verified': string;
            };
            'no-position-assigned': string;
            'tab': {
                admins: string;
                members: string;
                unknown: string;
            };
        };
    };
};
export default value;
