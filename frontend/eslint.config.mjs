import vueTsEslintConfig from '@vue/eslint-config-typescript';
import eslintConfigPrettier from 'eslint-config-prettier';
import pluginVue from 'eslint-plugin-vue';
import tseslint from 'typescript-eslint';

export default [
    ...tseslint.configs.strict,
    ...tseslint.configs.stylistic,
    ...pluginVue.configs['flat/recommended'],
    ...vueTsEslintConfig(),
    eslintConfigPrettier,
    {
        rules: {
            '@typescript-eslint/unified-signatures': 'off',
        },
    },
];
