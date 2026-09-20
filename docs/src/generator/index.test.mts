import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {promises as fs} from 'node:fs';
import path from 'node:path';
import test from 'node:test';

import JSZip from 'jszip';

import {
    generateStonecraftProject,
    type GenerateStonecraftProjectOptions,
} from './index.ts';

const ORIGINAL_FETCH = globalThis.fetch;
const RELEASE_URL = 'https://api.github.com/repos/meza/Stonecraft/releases/latest';
const OPTIONS: GenerateStonecraftProjectOptions = {
    modName: 'Sounds Be Gone',
    modId: 'soundsbegone',
    group: 'gg.meza',
    author: 'Meza',
    repository: 'https://codeberg.org/meza/SoundsBeGone',
    loaders: {
        fabric: true,
        forge: true,
        neoForge: true,
    },
    features: {
        dataGeneration: true,
        gameTests: true,
        publishing: true,
        automatedReleases: true,
        renovate: true,
    },
};

test.afterEach(() => {
    globalThis.fetch = ORIGINAL_FETCH;
});

function mockTemplate(bytes: Uint8Array): void {
    globalThis.fetch = (async (input) => {
        if (input === RELEASE_URL) {
            return Response.json({tag_name: 'v1.13.0'});
        }

        assert.equal(input, '/generator/template.zip');
        return new Response(new Blob([Uint8Array.from(bytes)]), {status: 200});
    }) as typeof fetch;
}

async function archiveTemplateDirectory(): Promise<Uint8Array> {
    const templateRoot = path.resolve('..', 'generator', 'template');
    const zip = new JSZip();

    async function visit(directory: string, prefix = ''): Promise<void> {
        const entries = await fs.readdir(directory, {withFileTypes: true});
        for (const entry of entries) {
            const archivePath = prefix ? `${prefix}/${entry.name}` : entry.name;
            const absolutePath = path.join(directory, entry.name);
            if (entry.isDirectory()) {
                await visit(absolutePath, archivePath);
            } else {
                const executable =
                    archivePath === 'gradlew' || /^scripts\/[^/]+\.sh$/.test(archivePath);
                zip.file(archivePath, await fs.readFile(absolutePath), {
                    binary: true,
                    createFolders: false,
                    date: new Date(Date.UTC(1980, 0, 1)),
                    unixPermissions: executable ? '755' : '644',
                });
            }
        }
    }

    await visit(templateRoot);
    return zip.generateAsync({type: 'uint8array', platform: 'UNIX'});
}

async function generate(source: Uint8Array, options = OPTIONS) {
    mockTemplate(source);
    return generateStonecraftProject(options);
}

test('renders the Stonecraft template', async () => {
    const source = await archiveTemplateDirectory();
    const first = await generate(source);
    const second = await generate(source);

    assert.equal(first.filename, 'soundsbegone.zip');
    assert.equal(first.archive.type, 'application/zip');

    const firstBytes = Buffer.from(await first.archive.arrayBuffer());
    const secondBytes = Buffer.from(await second.archive.arrayBuffer());
    assert.equal(
        createHash('sha256').update(firstBytes).digest('hex'),
        createHash('sha256').update(secondBytes).digest('hex'),
    );

    const zip = await JSZip.loadAsync(firstBytes);
    const files = Object.values(zip.files);
    assert.ok(zip.file('src/main/java/gg/meza/soundsbegone/SoundsBeGone.java'));
    assert.ok(zip.file('src/main/resources/soundsbegone.accesswidener'));
    assert.ok(zip.file('src/main/resources/assets/soundsbegone/icon.png'));
    assert.ok(zip.file('src/main/resources/data/soundsbegone/test_instance/noop.json'));
    assert.ok(zip.file('scripts/datagen.sh'));
    assert.ok(zip.file('scripts/release.sh'));
    assert.ok(zip.file('.releaserc.json'));
    assert.ok(zip.file('renovate.json'));

    const properties = await zip.file('gradle.properties')!.async('string');
    assert.match(properties, /mod\.id=soundsbegone/);
    assert.match(properties, /mod\.name=Sounds Be Gone/);
    assert.match(properties, /mod\.group=gg\.meza/);
    assert.match(properties, /mod\.description=A Minecraft mod called Sounds Be Gone\./);

    const settings = await zip.file('settings.gradle.kts')!.async('string');
    assert.match(settings, /id\("gg\.meza\.stonecraft"\) version "1\.13\.0"/);

    const fabricMetadata = JSON.parse(
        await zip.file('src/main/resources/fabric.mod.json')!.async('string'),
    );
    assert.deepEqual(fabricMetadata.authors, ['Meza']);
    assert.deepEqual(fabricMetadata.contact, {
        sources: 'https://codeberg.org/meza/SoundsBeGone',
    });
    assert.deepEqual(fabricMetadata.entrypoints.main, ['${group}.${id}.SoundsBeGone']);

    const neoForgeMetadata = await zip
        .file('src/main/resources/META-INF/neoforge.mods.toml')!
        .async('string');
    assert.ok(neoForgeMetadata.indexOf('[[mods]]') < neoForgeMetadata.indexOf('displayURL'));
    assert.match(neoForgeMetadata, /displayURL = "https:\/\/codeberg\.org\/meza\/SoundsBeGone"/);

    const forgeMetadata = await zip.file('src/main/resources/META-INF/mods.toml')!.async('string');
    assert.match(forgeMetadata, /authors = "Meza"/);

    const java = await zip
        .file('src/main/java/gg/meza/soundsbegone/SoundsBeGone.java')!
        .async('string');
    assert.match(java, /package gg\.meza\.soundsbegone;/);
    assert.match(java, /public class SoundsBeGone/);

    const license = await zip.file('LICENSE')!.async('string');
    assert.match(license, new RegExp(`Copyright \\(c\\) ${new Date().getUTCFullYear()} Meza`));

    const icon = await zip
        .file('src/main/resources/assets/soundsbegone/icon.png')!
        .async('uint8array');
    const sourceIcon = await fs.readFile(
        path.resolve(
            '..',
            'generator',
            'template',
            'src',
            'main',
            'resources',
            'assets',
            '__STONECRAFT_MOD_ID__',
            'icon.png',
        ),
    );
    assert.deepEqual(Buffer.from(icon), sourceIcon);

    const wrapper = await zip.file('gradle/wrapper/gradle-wrapper.jar')!.async('uint8array');
    const sourceWrapper = await fs.readFile(
        path.resolve('..', 'generator', 'template', 'gradle', 'wrapper', 'gradle-wrapper.jar'),
    );
    assert.deepEqual(Buffer.from(wrapper), sourceWrapper);

    for (const file of files) {
        assert.doesNotMatch(file.name, /__STONECRAFT_/);
        assert.equal(
            Buffer.from(await file.async('uint8array')).includes(Buffer.from('__STONECRAFT_')),
            false,
        );
    }
    assert.equal((zip.file('gradlew')!.unixPermissions as number) & 0o777, 0o755);
});

test('omits every disabled project capability', async () => {
    const source = await archiveTemplateDirectory();
    const project = await generate(source, {
        ...OPTIONS,
        features: {
            dataGeneration: false,
            gameTests: false,
            publishing: false,
            automatedReleases: false,
            renovate: false,
        },
    });
    const zip = await JSZip.loadAsync(await project.archive.arrayBuffer());

    assert.equal(zip.file('scripts/datagen.sh'), null);
    assert.equal(zip.file('scripts/release.sh'), null);
    assert.equal(zip.file('.releaserc.json'), null);
    assert.equal(zip.file('renovate.json'), null);
    assert.equal(zip.file('src/main/java/gg/meza/soundsbegone/gametest/ExampleGameTests.java'), null);
    assert.equal(zip.file('src/main/java/gg/meza/soundsbegone/datagen/ExampleAdvancements.java'), null);
    assert.equal(zip.file('src/main/resources/data/soundsbegone/test_instance/noop.json'), null);

    const fabricMetadata = JSON.parse(
        await zip.file('src/main/resources/fabric.mod.json')!.async('string'),
    );
    assert.deepEqual(fabricMetadata.entrypoints, {main: ['${group}.${id}.SoundsBeGone']});

    const build = await zip.file('build.gradle.kts')!.async('string');
    assert.doesNotMatch(build, /publishMods|CLIENT_OR_SERVER_PREFERS_BOTH/);
    assert.match(build, /import gg\.meza\.stonecraft\.mod/);

    const workflow = await zip.file('.github/workflows/build.yml')!.async('string');
    assert.doesNotMatch(workflow, /runDatagen|runGameTestServer|semantic-release|DO_PUBLISH/);
    assert.match(workflow, /\.\/gradlew buildAndCollect --stacktrace/);

    const readme = await zip.file('README.md')!.async('string');
    assert.doesNotMatch(readme, /DataGen|GameTest|Publishing configuration|semantic-release|Renovate/);
});

test('includes only selected mod loaders', async () => {
    const source = await archiveTemplateDirectory();
    const project = await generate(source, {
        ...OPTIONS,
        loaders: {fabric: false, forge: true, neoForge: false},
    });
    const zip = await JSZip.loadAsync(await project.archive.arrayBuffer());

    assert.equal(zip.file('src/main/resources/fabric.mod.json'), null);
    assert.ok(zip.file('src/main/resources/META-INF/mods.toml'));
    assert.equal(zip.file('src/main/resources/META-INF/neoforge.mods.toml'), null);
    assert.equal(
        zip.file('src/main/java/gg/meza/soundsbegone/datagen/fabric/ExampleDataGenerator.java'),
        null,
    );
    assert.equal(
        zip.file('src/main/java/gg/meza/soundsbegone/datagen/neoforge/ExampleDataGenerator.java'),
        null,
    );

    const readme = await zip.file('README.md')!.async('string');
    assert.match(readme, /build for Forge/);
});

test('requires at least one mod loader', async () => {
    const source = await archiveTemplateDirectory();

    await assert.rejects(
        generate(source, {
            ...OPTIONS,
            loaders: {fabric: false, forge: false, neoForge: false},
        }),
        /Select at least one mod loader/,
    );
});

test('omits repository metadata when no repository URL is provided', async () => {
    const source = await archiveTemplateDirectory();
    const {repository: _, ...withoutRepository} = OPTIONS;
    const project = await generate(source, withoutRepository);
    const zip = await JSZip.loadAsync(await project.archive.arrayBuffer());

    const fabricMetadata = JSON.parse(
        await zip.file('src/main/resources/fabric.mod.json')!.async('string'),
    );
    assert.equal('contact' in fabricMetadata, false);

    const neoForgeMetadata = await zip
        .file('src/main/resources/META-INF/neoforge.mods.toml')!
        .async('string');
    assert.doesNotMatch(neoForgeMetadata, /displayURL|issueTrackerURL/);

    const readme = await zip.file('README.md')!.async('string');
    assert.doesNotMatch(readme, /Source code is available at/);
});

test('keeps publishing and automated releases independently selectable', async () => {
    const source = await archiveTemplateDirectory();
    const releases = await generate(source, {
        ...OPTIONS,
        features: {...OPTIONS.features, publishing: false},
    });
    const releaseZip = await JSZip.loadAsync(await releases.archive.arrayBuffer());
    assert.ok(releaseZip.file('.releaserc.json'));
    assert.doesNotMatch(
        await releaseZip.file('scripts/release.sh')!.async('string'),
        /publishMods/,
    );
    assert.doesNotMatch(
        await releaseZip.file('.github/workflows/build.yml')!.async('string'),
        /DO_PUBLISH|MODRINTH_ID|CURSEFORGE_ID/,
    );

    const publishing = await generate(source, {
        ...OPTIONS,
        features: {...OPTIONS.features, automatedReleases: false},
    });
    const publishingZip = await JSZip.loadAsync(await publishing.archive.arrayBuffer());
    assert.equal(publishingZip.file('.releaserc.json'), null);
    assert.equal(publishingZip.file('scripts/release.sh'), null);
    assert.match(await publishingZip.file('build.gradle.kts')!.async('string'), /publishMods/);
    assert.match(await publishingZip.file('README.md')!.async('string'), /Publishing configuration/);
});

test('derives entrypoint class names', async () => {
    const template = new JSZip();
    template.file(
        '__STONECRAFT_ENTRYPOINT_CLASS__.txt',
        [
            '__STONECRAFT_ENTRYPOINT_CLASS__',
            '__STONECRAFT_#MOD_ID__enabled__STONECRAFT_/MOD_ID__',
            '__STONECRAFT_^UNKNOWN__disabled__STONECRAFT_/UNKNOWN__',
            '${{ github.ref_name }}',
        ].join('\n'),
        {
            createFolders: false,
            date: new Date(Date.UTC(1980, 0, 1)),
            unixPermissions: '644',
        },
    );
    const source = await template.generateAsync({type: 'uint8array', platform: 'UNIX'});

    const numeric = await JSZip.loadAsync(
        await (await generate(source, {...OPTIONS, modName: '7 Days'})).archive.arrayBuffer(),
    );
    assert.equal(
        await numeric.file('Mod7Days.txt')!.async('string'),
        ['Mod7Days', 'enabled', 'disabled', '${{ github.ref_name }}'].join('\n'),
    );

    const fallback = await JSZip.loadAsync(
        await (
            await generate(source, {...OPTIONS, modName: '💎', modId: 'diamond_mod'})
        ).archive.arrayBuffer(),
    );
    assert.equal(
        await fallback.file('DiamondMod.txt')!.async('string'),
        ['DiamondMod', 'enabled', 'disabled', '${{ github.ref_name }}'].join('\n'),
    );
});

test('reports an unsuccessful template response', async () => {
    globalThis.fetch = (async (input) =>
        input === RELEASE_URL
            ? Response.json({tag_name: 'v1.13.0'})
            : new Response(null, {status: 503})) as typeof fetch;

    await assert.rejects(generateStonecraftProject(OPTIONS), /HTTP 503/);
});

test('reports an unsuccessful latest release response', async () => {
    globalThis.fetch = (async (input) => {
        assert.equal(input, RELEASE_URL);
        return new Response(null, {status: 503});
    }) as typeof fetch;

    await assert.rejects(generateStonecraftProject(OPTIONS), /latest Stonecraft release: HTTP 503/);
});

test('rejects a latest release without a usable tag', async () => {
    for (const tag_name of [undefined, 'v']) {
        globalThis.fetch = (async (input) => {
            assert.equal(input, RELEASE_URL);
            return Response.json({tag_name});
        }) as typeof fetch;

        await assert.rejects(generateStonecraftProject(OPTIONS), /usable tag_name/);
    }
});
