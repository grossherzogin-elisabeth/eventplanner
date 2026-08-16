import fs from 'node:fs';
import path from 'node:path';
import yaml from 'yaml';

function toType(value: unknown): string {
    if (Array.isArray(value)) {
        if (value.length === 0) return 'unknown[]';
        return `Array<${toType(value[0])}>`;
    }

    if (value === null) return 'null';

    switch (typeof value) {
        case 'string':
            return 'string';
        case 'number':
            return 'number';
        case 'boolean':
            return 'boolean';
        case 'object': {
            const entries = Object.entries(value as Record<string, unknown>);
            const fields = entries.map(([key, val]) => `  ${JSON.stringify(key)}: ${toType(val)};`);
            return `{\n${fields.join('\n')}\n}`;
        }
        default:
            return 'unknown';
    }
}

const inputFile = path.resolve('src/ui/locales/de.yaml');
const outputFile = `${inputFile}.d.ts`;

const content = fs.readFileSync(inputFile, 'utf8');
const parsed = yaml.parse(content);
const typeBody = toType(parsed);

const dts = `declare const value: ${typeBody};
export default value;
`;

fs.writeFileSync(outputFile, dts);
console.log(`Generated ${outputFile}`);
