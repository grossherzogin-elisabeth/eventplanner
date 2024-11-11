import {
    Blend,
    DynamicColor,
    DynamicScheme,
    Hct,
    SchemeAndroid,
    SchemeContent,
    SchemeExpressive,
    SchemeFidelity,
    SchemeFruitSalad,
    SchemeMonochrome,
    SchemeRainbow,
    SchemeTonalSpot,
    SchemeVibrant,
    TonalPalette,
    argbFromHex,
    hexFromArgb,
} from '@material/material-color-utilities';

let darkMode = true;
darkMode = false;

let hct = Hct.fromInt(argbFromHex('#188edc'));
// hct = Hct.fromInt(argbFromHex('#007800'));
const scheme = new SchemeTonalSpot(hct, darkMode, 0);
// const scheme = new DynamicScheme({
//     sourceColorArgb: argbFromHex('#188edc'),
//     variant: 2,
//     isDark: darkMode,
//     contrastLevel: 0.0,
//     // primaryPalette: TonalPalette.fromInt(argbFromHex('#4c7ba7')),
//     primaryPalette: TonalPalette.fromInt(argbFromHex('#188edc')),
//     // secondaryPalette: TonalPalette.fromInt(argbFromHex('#6a8889')),
//     secondaryPalette: TonalPalette.fromInt(argbFromHex('#c4deef')),
//     // tertiaryPalette: TonalPalette.fromInt(argbFromHex('#827093')),
//     tertiaryPalette: TonalPalette.fromInt(argbFromHex('#bf805e')),
//     neutralPalette: TonalPalette.fromInt(argbFromHex('#74777c')),
//     neutralVariantPalette: TonalPalette.fromInt(argbFromHex('#72777e')),
// });

/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx,vue}'],
    theme: {
        colors: ({ colors }) => ({
            // generic colors
            current: 'currentColor',
            transparent: 'transparent',
            black: '#000000',
            white: '#ffffff',
            inherit: 'inherit',
            gray: colors.stone,
            onblue: '#0C476E',
            blue: {
                DEFAULT: '#D1EAFA',
                50: 'hsl(204,80%,97%)',
                100: 'hsl(204,80%,94%)',
                200: 'hsl(204,80%,90%)',
                300: 'hsl(204,80%,74%)',
                400: 'hsl(204,80%,60%)',
                500: 'hsl(204,80%,48%)',
                600: 'hsl(204,80%,39%)',
                700: 'hsl(204,80%,32%)',
                800: 'hsl(204,80%,27%)',
                900: 'hsl(204,80%,24%)',
                950: 'hsl(204,80%,16%)',
            },
            onyellow: '#713f12',
            yellow: {
                DEFAULT: '#fde68a',
                ...colors.yellow,
            },
            green: {
                DEFAULT: '#C6ECD3',
                50: 'hsl(138,50%,97%)',
                100: 'hsl(141,50%,93%)',
                200: 'hsl(141,50%,85%)',
                300: 'hsl(142,50%,73%)',
                400: 'hsl(142,69%,58%)',
                500: 'hsl(142,71%,45%)',
                600: 'hsl(142,76%,36%)',
                700: 'hsl(142,72%,29%)',
                800: 'hsl(143,64%,24%)',
                900: '#14522d',
                950: 'hsl(145,80%,10%)',
            },
            ongreen: '#14532d',

            p: {
                50: hexFromArgb(scheme.primaryPalette.tone(95)),
                100: hexFromArgb(scheme.primaryPalette.tone(90)),
                200: hexFromArgb(scheme.primaryPalette.tone(80)),
                300: hexFromArgb(scheme.primaryPalette.tone(70)),
                400: hexFromArgb(scheme.primaryPalette.tone(60)),
                500: hexFromArgb(scheme.primaryPalette.tone(50)),
                600: hexFromArgb(scheme.primaryPalette.tone(40)),
                700: hexFromArgb(scheme.primaryPalette.tone(30)),
                800: hexFromArgb(scheme.primaryPalette.tone(20)),
                900: hexFromArgb(scheme.primaryPalette.tone(10)),
                950: hexFromArgb(scheme.primaryPalette.tone(5)),
            },
            s: {
                50: hexFromArgb(scheme.secondaryPalette.tone(5)),
                100: hexFromArgb(scheme.secondaryPalette.tone(10)),
                200: hexFromArgb(scheme.secondaryPalette.tone(20)),
                300: hexFromArgb(scheme.secondaryPalette.tone(30)),
                400: hexFromArgb(scheme.secondaryPalette.tone(40)),
                500: hexFromArgb(scheme.secondaryPalette.tone(50)),
                600: hexFromArgb(scheme.secondaryPalette.tone(60)),
                700: hexFromArgb(scheme.secondaryPalette.tone(70)),
                800: hexFromArgb(scheme.secondaryPalette.tone(80)),
                900: hexFromArgb(scheme.secondaryPalette.tone(90)),
                950: hexFromArgb(scheme.secondaryPalette.tone(95)),
            },
            t: {
                50: hexFromArgb(scheme.tertiaryPalette.tone(5)),
                100: hexFromArgb(scheme.tertiaryPalette.tone(10)),
                200: hexFromArgb(scheme.tertiaryPalette.tone(20)),
                300: hexFromArgb(scheme.tertiaryPalette.tone(30)),
                400: hexFromArgb(scheme.tertiaryPalette.tone(40)),
                500: hexFromArgb(scheme.tertiaryPalette.tone(50)),
                600: hexFromArgb(scheme.tertiaryPalette.tone(60)),
                700: hexFromArgb(scheme.tertiaryPalette.tone(70)),
                800: hexFromArgb(scheme.tertiaryPalette.tone(80)),
                900: hexFromArgb(scheme.tertiaryPalette.tone(90)),
                950: hexFromArgb(scheme.tertiaryPalette.tone(95)),
            },
            n: {
                50: hexFromArgb(scheme.neutralPalette.tone(5)),
                100: hexFromArgb(scheme.neutralPalette.tone(10)),
                200: hexFromArgb(scheme.neutralPalette.tone(20)),
                300: hexFromArgb(scheme.neutralPalette.tone(30)),
                400: hexFromArgb(scheme.neutralPalette.tone(40)),
                500: hexFromArgb(scheme.neutralPalette.tone(50)),
                600: hexFromArgb(scheme.neutralPalette.tone(60)),
                700: hexFromArgb(scheme.neutralPalette.tone(70)),
                800: hexFromArgb(scheme.neutralPalette.tone(80)),
                900: hexFromArgb(scheme.neutralPalette.tone(90)),
                950: hexFromArgb(scheme.neutralPalette.tone(95)),
            },
            e: {
                50: hexFromArgb(scheme.errorPalette.tone(5)),
                100: hexFromArgb(scheme.errorPalette.tone(10)),
                200: hexFromArgb(scheme.errorPalette.tone(20)),
                300: hexFromArgb(scheme.errorPalette.tone(30)),
                400: hexFromArgb(scheme.errorPalette.tone(40)),
                500: hexFromArgb(scheme.errorPalette.tone(50)),
                600: hexFromArgb(scheme.errorPalette.tone(60)),
                700: hexFromArgb(scheme.errorPalette.tone(70)),
                800: hexFromArgb(scheme.errorPalette.tone(80)),
                900: hexFromArgb(scheme.errorPalette.tone(90)),
                950: hexFromArgb(scheme.errorPalette.tone(95)),
            },

            error: {
                DEFAULT: hexFromArgb(scheme.error),
                container: hexFromArgb(scheme.errorContainer),
            },
            onerror: {
                DEFAULT: hexFromArgb(scheme.onError),
                container: hexFromArgb(scheme.onErrorContainer),
            },
            onprimary: {
                DEFAULT: hexFromArgb(scheme.onPrimary),
                container: hexFromArgb(scheme.onPrimaryContainer),
                fixed: hexFromArgb(scheme.onPrimaryFixed),
                variant: hexFromArgb(scheme.onPrimaryFixedVariant),
            },
            primary: {
                DEFAULT: hexFromArgb(scheme.primary),
                container: hexFromArgb(scheme.primaryContainer),
                fixed: hexFromArgb(scheme.primaryFixed),
                variant: hexFromArgb(scheme.primaryFixedDim),

                50: '#f1f9fd',
                100: 'hsl(204,80%,94%)',
                200: 'hsl(204,80%,90%)',
                300: 'hsl(204,80%,85%)',
                400: '#75bff0',
                500: '#188edc',
                600: 'hsl(204,80%,39%)',
                700: 'hsl(204,80%,32%)',
                800: 'hsl(204,80%,27%)',
                900: 'hsl(204,80%,24%)',
                950: 'hsl(204,80%,16%)',

                // 50: hexFromArgb(scheme.primaryPalette.tone(95)),
                // 100: hexFromArgb(scheme.primaryPalette.tone(90)),
                // 200: hexFromArgb(scheme.primaryPalette.tone(80)),
                // 300: hexFromArgb(scheme.primaryPalette.tone(70)),
                // 400: hexFromArgb(scheme.primaryPalette.tone(60)),
                // 500: hexFromArgb(scheme.primaryPalette.tone(50)),
                // 600: hexFromArgb(scheme.primaryPalette.tone(40)),
                // 700: hexFromArgb(scheme.primaryPalette.tone(30)),
                // 800: hexFromArgb(scheme.primaryPalette.tone(20)),
                // 900: hexFromArgb(scheme.primaryPalette.tone(10)),
                // 950: hexFromArgb(scheme.primaryPalette.tone(5)),
            },
            secondary: {
                DEFAULT: hexFromArgb(scheme.secondary),
                container: hexFromArgb(scheme.secondaryContainer),
                fixed: hexFromArgb(scheme.secondaryFixed),
                variant: hexFromArgb(scheme.secondaryFixedDim),
            },
            onsecondary: {
                DEFAULT: hexFromArgb(scheme.onSecondary),
                container: hexFromArgb(scheme.onSecondaryContainer),
                fixed: hexFromArgb(scheme.onSecondaryFixed),
                variant: hexFromArgb(scheme.onSecondaryFixedVariant),
            },
            tertiary: {
                DEFAULT: hexFromArgb(scheme.tertiary),
                container: hexFromArgb(scheme.tertiaryContainer),
                fixed: hexFromArgb(scheme.tertiaryFixed),
                variant: hexFromArgb(scheme.tertiaryFixedDim),
            },
            ontertiary: {
                DEFAULT: hexFromArgb(scheme.onTertiary),
                container: hexFromArgb(scheme.onTertiaryContainer),
                fixed: hexFromArgb(scheme.onTertiaryFixed),
                variant: hexFromArgb(scheme.onTertiaryFixedVariant),
            },
            surface: {
                'dim': hexFromArgb(scheme.surfaceDim),
                'DEFAULT': hexFromArgb(scheme.surface),
                'bright': hexFromArgb(scheme.surfaceBright),
                'container-lowest': hexFromArgb(scheme.surfaceContainerLowest),
                'container-low': hexFromArgb(scheme.surfaceContainerLow),
                'container': hexFromArgb(scheme.surfaceContainer),
                'container-high': hexFromArgb(scheme.surfaceContainerHigh),
                'container-highest': hexFromArgb(scheme.surfaceContainerHighest),
                'variant': hexFromArgb(scheme.surfaceVariant),
                'tint': hexFromArgb(scheme.surfaceTint),
            },
            onsurface: {
                DEFAULT: hexFromArgb(scheme.onSurface),
                variant: hexFromArgb(scheme.onSurfaceVariant),
            },
            outline: {
                DEFAULT: hexFromArgb(scheme.outline),
                variant: hexFromArgb(scheme.outlineVariant),
            },
        }),
        extend: {
            screens: {
                xxl: 1600,
            },
            // boxShadow: {
            //     sm: '0 0px 2px rgb(0 0 0 / 0.05)',
            //     DEFAULT: ['0 0px 3px rgb(0 0 0 / 0.1)', '0 0px 2px rgb(0 0 0 / 0.06)'],
            //     md: ['0 0px 8px rgb(0 0 0 / 0.07)', '0 0px 3px rgb(0 0 0 / 0.06)'],
            //     lg: ['0 0px 13px rgb(0 0 0 / 0.04)', '0 0px 5px rgb(0 0 0 / 0.1)'],
            // },
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
        },
    },
    plugins: [],
};
