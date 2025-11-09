import { deepCopy, hash } from '@/common/utils/ObjectUtils';
import { describe, expect, it } from 'vitest';

describe('ObjectUtils', () => {
    describe('hash', () => {
        it('should return same hash for same input string', () => {
            expect(hash('somestring')).toEqual(hash('somestring'));
        });

        it('should return different hash for different input strings', () => {
            expect(hash('somestring')).not.toEqual(hash('somestring else'));
        });

        it('should return same hash for same input object', () => {
            expect(hash({ a: 'somestring', b: 123 })).toEqual(hash({ a: 'somestring', b: 123 }));
        });

        it('should return different hash for same different input objects', () => {
            expect(hash({ a: 'somestring', b: 123 })).not.toEqual(hash({ a: 'somestring else', b: 123 }));
        });
    });

    describe('deepCopy', () => {
        interface TestType {
            num: number;
            bool: boolean;
            str: string;
            obj: {
                key: string;
                a: string;
                b: string;
                c: string;
            };
            list: string[];
            objList: {
                key: string;
                a: string;
                b: string;
                c: string;
            }[];
            diverseList: (string | number)[];
        }

        function createTestType(): TestType {
            return {
                num: 1,
                bool: true,
                str: 'test',
                obj: {
                    key: 'key',
                    a: 'test a',
                    b: 'test b',
                    c: 'test c',
                },
                list: ['a', 'b', 'c'],
                objList: [
                    {
                        key: 'key',
                        a: 'test a',
                        b: 'test b',
                        c: 'test c',
                    },
                ],
                diverseList: [1, '2', 3, '4'],
            };
        }

        it('should copy all fields', async () => {
            const original = createTestType();
            const copy = deepCopy(original);
            expect(copy).toEqual(original);
        });

        it('should have no reference in copy', async () => {
            const original = createTestType();
            const copy = deepCopy(original);

            copy.obj.b = 'changed';
            copy.list.push('changed');
            copy.diverseList = [];
            copy.objList[0].a = 'changed';

            expect(copy).not.toEqual(original);
        });
    });
});
