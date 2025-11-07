import {
    Hct,
    SchemeTonalSpot,
    argbFromHex,
    blueFromArgb,
    customColor,
    greenFromArgb,
    hexFromArgb,
    redFromArgb,
} from '@material/material-color-utilities';

export function generateColorSchemeCssVariables(baseColor: string, darkMode: boolean, contrast: 0 | 1 | 2): string[] {
    const argb = argbFromHex(baseColor);
    const scheme = new SchemeTonalSpot(Hct.fromInt(argb), darkMode, contrast);

    console.log(`
    primary: '${hexFromArgb(scheme.primaryContainer)}',
    secondary: '${hexFromArgb(scheme.secondaryContainer)}',
    tertiary: '${hexFromArgb(scheme.tertiaryContainer)}',
    `);

    const colors = [
        `--color-primary: ${renderColor(scheme.primary)};`,
        `--color-primary-container: ${renderColor(scheme.primaryContainer)};`,
        `--color-primary-fixed: ${renderColor(scheme.primaryFixed)};`,
        `--color-primary-fixed-dim: ${renderColor(scheme.primaryFixedDim)};`,
        `--color-onprimary: ${renderColor(scheme.onPrimary)};`,
        `--color-onprimary-container: ${renderColor(scheme.onPrimaryContainer)};`,
        `--color-onprimary-fixed: ${renderColor(scheme.onPrimaryFixed)};`,
        `--color-onprimary-fixed-variant: ${renderColor(scheme.onPrimaryFixedVariant)};`,

        `--color-secondary: ${renderColor(scheme.secondary)};`,
        `--color-secondary-container: ${renderColor(scheme.secondaryContainer)};`,
        `--color-secondary-fixed: ${renderColor(scheme.secondaryFixed)};`,
        `--color-secondary-fixed-dim: ${renderColor(scheme.secondaryFixedDim)};`,
        `--color-onsecondary: ${renderColor(scheme.onSecondary)};`,
        `--color-onsecondary-container: ${renderColor(scheme.onSecondaryContainer)};`,
        `--color-onsecondary-fixed: ${renderColor(scheme.onSecondaryFixed)};`,
        `--color-onsecondary-fixed-variant: ${renderColor(scheme.onSecondaryFixedVariant)};`,

        `--color-tertiary: ${renderColor(scheme.tertiary)};`,
        `--color-tertiary-container: ${renderColor(scheme.tertiaryContainer)};`,
        `--color-tertiary-fixed: ${renderColor(scheme.tertiaryFixed)};`,
        `--color-tertiary-fixed-dim: ${renderColor(scheme.tertiaryFixedDim)};`,
        `--color-ontertiary: ${renderColor(scheme.onTertiary)};`,
        `--color-ontertiary-container: ${renderColor(scheme.onTertiaryContainer)};`,
        `--color-ontertiary-fixed: ${renderColor(scheme.onTertiaryFixed)};`,
        `--color-ontertiary-fixed-variant: ${renderColor(scheme.onTertiaryFixedVariant)};`,

        `--color-surface: ${renderColor(scheme.surface)};`,
        `--color-surface-dim: ${renderColor(scheme.surfaceDim)};`,
        `--color-surface-bright: ${renderColor(scheme.surfaceBright)};`,
        `--color-surface-variant: ${renderColor(scheme.surfaceVariant)};`,
        `--color-surface-tint: ${renderColor(scheme.surfaceTint)};`,
        `--color-surface-container-lowest: ${renderColor(scheme.surfaceContainerLowest)};`,
        `--color-surface-container-low: ${renderColor(scheme.surfaceContainerLow)};`,
        `--color-surface-container: ${renderColor(scheme.surfaceContainer)};`,
        `--color-surface-container-high: ${renderColor(scheme.surfaceContainerHigh)};`,
        `--color-surface-container-highest: ${renderColor(scheme.surfaceContainerHighest)};`,
        `--color-onsurface: ${renderColor(scheme.onSurface)};`,
        `--color-onsurface-variant: ${renderColor(scheme.onSurfaceVariant)};`,

        `--color-outline: ${renderColor(scheme.outline)};`,
        `--color-outline-variant: ${renderColor(scheme.outlineVariant)};`,
        `--color-scrim: ${renderColor(scheme.scrim)};`,

        // `--color-error: ${renderColor(scheme.error)};`,
        // `--color-error-container: ${renderColor(scheme.errorContainer)};`,
        // `--color-onerror: ${renderColor(scheme.onError)};`,
        // `--color-onerror-container: ${renderColor(scheme.onErrorContainer)};`,
    ];

    generateCustomColor(argb, 'success', argbFromHex('#3f6212'), darkMode, true).forEach((c) => colors.push(c));
    generateCustomColor(argb, 'warning', argbFromHex('#eab308'), darkMode, true).forEach((c) => colors.push(c));
    generateCustomColor(argb, 'info', argbFromHex('#188edc'), darkMode, true).forEach((c) => colors.push(c));
    generateCustomColor(argb, 'error', argbFromHex('#b91c1c'), darkMode, true).forEach((c) => colors.push(c));

    return colors;
}

function renderColor(color: number | string): string {
    let argb = 0;
    if (typeof color === 'string') {
        argb = argbFromHex(color);
    } else if (typeof color === 'number') {
        argb = color;
    }
    return `rgb(${redFromArgb(argb)} ${greenFromArgb(argb)} ${blueFromArgb(argb)})`;
}

function generateCustomColor(themeColor: number, name: string, color: number, darkMode: boolean, blend: boolean): string[] {
    const customColorGroup = customColor(themeColor, { value: color, name: name, blend: blend });
    const c = darkMode ? customColorGroup.dark : customColorGroup.light;
    return [
        `--color-${name}: ${renderColor(c.color)};`,
        `--color-${name}-container: ${renderColor(c.colorContainer)};`,
        `--color-on${name}: ${renderColor(c.onColor)};`,
        `--color-on${name}-container: ${renderColor(c.onColorContainer)};`,
    ];
}
