import { vueTsConfigs, withVueTs } from '@vue/eslint-config-typescript';
import eslintConfigPrettier from 'eslint-config-prettier';
import eslintPluginVue from 'eslint-plugin-vue';

export default withVueTs(eslintPluginVue.configs['flat/recommended'], vueTsConfigs.recommended, eslintConfigPrettier, {
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
});
