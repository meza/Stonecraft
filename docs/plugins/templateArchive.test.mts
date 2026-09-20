import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {mkdtemp, mkdir, rm, symlink, writeFile} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import path from 'node:path';
import {spawnSync} from 'node:child_process';
import test from 'node:test';

import JSZip from 'jszip';
import webpack, {type Compiler} from 'webpack';

import {
    createTemplateArchive,
    templateArchivePlugin,
    TemplateArchiveWebpackPlugin,
} from './templateArchive.ts';

const EXPECTED_DATE = '1980-01-01T00:00:00.000Z';

async function createFixture(): Promise<string> {
    const directory = await mkdtemp(path.join(tmpdir(), 'stonecraft-template-'));
    await mkdir(path.join(directory, 'scripts'));
    await writeFile(path.join(directory, 'gradlew'), '#!/bin/sh\n');
    await writeFile(path.join(directory, 'scripts', 'release.sh'), '#!/bin/sh\n');
    await writeFile(path.join(directory, '.gitignore'), 'build/\n');
    await writeFile(path.join(directory, 'Z.txt'), 'uppercase\n');
    await writeFile(path.join(directory, 'a.txt'), 'lowercase\n');
    await writeFile(path.join(directory, 'ä.txt'), 'unicode\n');
    return directory;
}

async function withFixture(run: (directory: string) => Promise<void>): Promise<void> {
    const directory = await createFixture();
    try {
        await run(directory);
    } finally {
        await rm(directory, {recursive: true, force: true});
    }
}

function sha256(content: Buffer): string {
    return createHash('sha256').update(content).digest('hex');
}

test('creates a deterministic archive with exact content and permissions', async () => {
    await withFixture(async (directory) => {
        const first = await createTemplateArchive(directory);
        const second = await createTemplateArchive(directory);
        assert.equal(sha256(first.archive), sha256(second.archive));

        const zip = await JSZip.loadAsync(first.archive);
        const entries = Object.values(zip.files).filter((entry) => !entry.dir);
        assert.deepEqual(
            entries.map((entry) => entry.name),
            ['.gitignore', 'Z.txt', 'a.txt', 'gradlew', 'scripts/release.sh', 'ä.txt'],
        );

        for (const entry of entries) {
            assert.equal(entry.date.toISOString(), EXPECTED_DATE);
            const expected = await import('node:fs/promises').then((fs) =>
                fs.readFile(path.join(directory, ...entry.name.split('/'))),
            );
            assert.deepEqual(await entry.async('nodebuffer'), expected);
            const permissions =
                typeof entry.unixPermissions === 'string'
                    ? Number.parseInt(entry.unixPermissions, 8)
                    : entry.unixPermissions!;
            assert.equal(
                permissions & 0o777,
                entry.name === 'gradlew' || entry.name.endsWith('.sh') ? 0o755 : 0o644,
            );
        }
    });
});

test('creates identical archives in different time zones', async () => {
    await withFixture(async (directory) => {
        const moduleUrl = new URL('./templateArchive.ts', import.meta.url).href;
        const script = `
            import {createHash} from 'node:crypto';
            import {createTemplateArchive} from ${JSON.stringify(moduleUrl)};
            const {archive} = await createTemplateArchive(process.env.TEMPLATE_DIRECTORY);
            process.stdout.write(createHash('sha256').update(archive).digest('hex'));
        `;

        const hashes = ['America/Los_Angeles', 'Asia/Tokyo'].map((timezone) => {
            const result = spawnSync(process.execPath, ['--input-type=module', '--eval', script], {
                cwd: process.cwd(),
                encoding: 'utf8',
                env: {...process.env, TEMPLATE_DIRECTORY: directory, TZ: timezone},
            });
            assert.equal(result.status, 0, result.stderr);
            return result.stdout;
        });

        assert.equal(hashes[0], hashes[1]);
    });
});

test('rejects empty templates', async () => {
    const directory = await mkdtemp(path.join(tmpdir(), 'stonecraft-template-empty-'));
    try {
        await assert.rejects(createTemplateArchive(directory), /template is empty/);
    } finally {
        await rm(directory, {recursive: true, force: true});
    }
});

test('rejects Git metadata at any depth', async () => {
    for (const relativePath of ['.git', path.join('nested', '.git')]) {
        const directory = await mkdtemp(path.join(tmpdir(), 'stonecraft-template-git-'));
        try {
            await mkdir(path.join(directory, relativePath), {recursive: true});
            await assert.rejects(createTemplateArchive(directory), /must not contain Git metadata/);
        } finally {
            await rm(directory, {recursive: true, force: true});
        }
    }
});

test('rejects unsupported filesystem entries', async () => {
    const directory = await mkdtemp(path.join(tmpdir(), 'stonecraft-template-link-'));
    try {
        await writeFile(path.join(directory, 'target.txt'), 'target\n');
        await symlink(path.join(directory, 'target.txt'), path.join(directory, 'link.txt'));
        await assert.rejects(createTemplateArchive(directory), /Unsupported template entry/);
    } finally {
        await rm(directory, {recursive: true, force: true});
    }
});

test('emits the archive and registers source dependencies', async () => {
    await withFixture(async (directory) => {
        let compilationHandler: ((compilation: unknown) => void) | undefined;
        let processAssetsHandler: (() => Promise<void>) | undefined;
        let emittedName: string | undefined;
        let emittedSource: {buffer(): Buffer} | undefined;
        const contextDependencies = new Set<string>();
        const fileDependencies = new Set<string>();

        const compiler = {
            hooks: {
                thisCompilation: {
                    tap(name: string, handler: (compilation: unknown) => void) {
                        assert.equal(name, 'stonecraft-template-archive');
                        compilationHandler = handler;
                    },
                },
            },
            webpack,
        } as unknown as Compiler;

        new TemplateArchiveWebpackPlugin(directory).apply(compiler);
        assert.ok(compilationHandler);

        compilationHandler({
            contextDependencies,
            fileDependencies,
            hooks: {
                processAssets: {
                    tapPromise(
                        options: {name: string; stage: number},
                        handler: () => Promise<void>,
                    ) {
                        assert.equal(options.name, 'stonecraft-template-archive');
                        assert.equal(
                            options.stage,
                            webpack.Compilation.PROCESS_ASSETS_STAGE_ADDITIONAL,
                        );
                        processAssetsHandler = handler;
                    },
                },
            },
            emitAsset(name: string, source: {buffer(): Buffer}) {
                emittedName = name;
                emittedSource = source;
            },
        });

        assert.ok(processAssetsHandler);
        await processAssetsHandler();
        assert.equal(emittedName, 'generator/template.zip');
        assert.ok(emittedSource);
        assert.ok(emittedSource.buffer().length > 0);
        assert.deepEqual([...contextDependencies], [directory]);
        assert.equal(fileDependencies.size, 6);
    });
});

test('registers the archive emitter only for client compilations', () => {
    const context = {siteDir: path.resolve('docs')} as never;
    const plugin = templateArchivePlugin(context);
    const configureWebpack = plugin.configureWebpack!;

    assert.deepEqual(configureWebpack({} as never, true, {} as never, undefined), {});
    const clientConfig = configureWebpack({} as never, false, {} as never, undefined);
    assert.equal(clientConfig!.plugins!.length, 1);
    assert.ok(clientConfig!.plugins![0] instanceof TemplateArchiveWebpackPlugin);
});
