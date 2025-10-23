/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx,vue}'],
    darkMode: 'selector',
    theme: {
        // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
        colors: ({ colors }) => ({
            // generic colors
            current: 'currentColor',
            transparent: 'transparent',
            black: '#000000',
            white: '#ffffff',
            inherit: 'inherit',

            info: {
                DEFAULT: 'rgb(var(--color-info))',
                container: 'rgb(var(--color-info-container))',
            },
            oninfo: {
                DEFAULT: 'rgb(var(--color-oninfo))',
                container: 'rgb(var(--color-oninfo-container))',
            },
            warning: {
                DEFAULT: 'rgb(var(--color-warning))',
                container: 'rgb(var(--color-warning-container))',
                ...colors.yellow,
            },
            onwarning: {
                DEFAULT: 'rgb(var(--color-onwarning))',
                container: 'rgb(var(--color-onwarning-container))',
            },
            success: {
                DEFAULT: 'rgb(var(--color-success))',
                container: 'rgb(var(--color-success-container))',
            },
            onsuccess: {
                DEFAULT: 'rgb(var(--color-onsuccess))',
                container: 'rgb(var(--color-onsuccess-container))',
            },
            error: {
                DEFAULT: 'rgb(var(--color-error))',
                container: 'rgb(var(--color-error-container))',
            },
            onerror: {
                DEFAULT: 'rgb(var(--color-onerror))',
                container: 'rgb(var(--color-onerror-container))',
            },
            primary: {
                'DEFAULT': 'rgb(var(--color-primary))',
                'container': 'rgb(var(--color-primary-container))',
                'fixed': 'rgb(var(--color-primary-fixed))',
                'fixed-dim': 'rgb(var(--color-primary-fixed-dim))',
            },
            onprimary: {
                'DEFAULT': 'rgb(var(--color-onprimary))',
                'container': 'rgb(var(--color-onprimary-container))',
                'fixed': 'rgb(var(--color-onprimary-fixed))',
                'fixed-variant': 'rgb(var(--color-onprimary-fixed-variant))',
            },
            secondary: {
                'DEFAULT': 'rgb(var(--color-secondary))',
                'container': 'rgb(var(--color-secondary-container))',
                'fixed': 'rgb(var(--color-secondary-fixed))',
                'fixed-dim': 'rgb(var(--color-secondary-fixed-dim))',
            },
            onsecondary: {
                'DEFAULT': 'rgb(var(--color-onsecondary))',
                'container': 'rgb(var(--color-onsecondary-container))',
                'fixed': 'rgb(var(--color-onsecondary-fixed))',
                'fixed-variant': 'rgb(var(--color-onsecondary-fixed-variant))',
            },
            tertiary: {
                'DEFAULT': 'rgb(var(--color-tertiary))',
                'container': 'rgb(var(--color-tertiary-container))',
                'fixed': 'rgb(var(--color-tertiary-fixed))',
                'fixed-dim': 'rgb(var(--color-tertiary-fixed-dim))',
            },
            ontertiary: {
                'DEFAULT': 'rgb(var(--color-ontertiary))',
                'container': 'rgb(var(--color-ontertiary-container))',
                'fixed': 'rgb(var(--color-ontertiary-fixed))',
                'fixed-variant': 'rgb(var(--color-ontertiary-fixed-variant))',
            },
            surface: {
                'DEFAULT': 'rgb(var(--color-surface))',
                'dim': 'rgb(var(--color-surface-dim))',
                'bright': 'rgb(var(--color-surface-bright))',
                'container-lowest': 'rgb(var(--color-surface-container-lowest))',
                'container-low': 'rgb(var(--color-surface-container-low))',
                'container': 'rgb(var(--color-surface-container))',
                'container-high': 'rgb(var(--color-surface-container-high))',
                'container-highest': 'rgb(var(--color-surface-container-highest))',
                'variant': 'rgb(var(--color-surface-variant))',
                'tint': 'rgb(var(--color-surface-tint))',
            },
            onsurface: {
                DEFAULT: 'rgb(var(--color-onsurface))',
                variant: 'rgb(var(--color-onsurface-variant))',
            },
            outline: {
                DEFAULT: 'rgb(var(--color-outline))',
                variant: 'rgb(var(--color-outline-variant))',
            },
        }),
        extend: {
            borderRadius: {
                '4xl': '3em',
            },
            maxHeight: {
                'xs': '20rem',
                'sm': '24rem',
                'md': '28rem',
                'lg': '32rem',
                'xl': '36rem',
                '2xl': '42rem',
                '3xl': '48rem',
                '4xl': '56rem',
                '5xl': '64rem',
                '6xl': '72rem',
                '7xl': '80rem',
            },
            screens: {
                xs: '400px',
            },
        },
    },
    plugins: [],
};
