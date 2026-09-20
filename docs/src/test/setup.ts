import {cleanup} from '@testing-library/react';
import {afterEach, expect} from 'vitest';
import {toHaveNoViolations} from 'vitest-axe/matchers';
import 'vitest-axe/extend-expect';

expect.extend({toHaveNoViolations});

afterEach(() => {
    cleanup();
});
