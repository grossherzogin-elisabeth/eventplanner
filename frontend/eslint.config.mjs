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
            // allow to always explicitly define a type
            '@typescript-eslint/no-inferrable-types': 'off',
            // allow unused functions vars (for development), but show a warning
            '@typescript-eslint/no-unused-vars': 'warn',
            '@typescript-eslint/no-empty-function': 'warn',
            // automatically add type import
            '@typescript-eslint/consistent-type-imports': 'error',
            '@typescript-eslint/explicit-function-return-type': ['error'],
        },
    },
];
