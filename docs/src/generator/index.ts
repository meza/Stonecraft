import JSZip from 'jszip';
import Mustache from 'mustache';

const TEMPLATE_URL = '/generator/template.zip';
const MUSTACHE_OPTIONS = {
    escape: String,
    tags: ['__STONECRAFT_', '__'] as [string, string],
};

/** Optional capabilities included in a generated Stonecraft project. */
export type StonecraftProjectFeatures = {
    dataGeneration: boolean;
    gameTests: boolean;
    publishing: boolean;
    automatedReleases: boolean;
    renovate: boolean;
};

/** Values used to render a new Stonecraft project. */
export type GenerateStonecraftProjectOptions = {
    modName: string;
    modId: string;
    group: string;
    author: string;
    repository?: string;
    features: StonecraftProjectFeatures;
};

/** Browser-downloadable result of generating a Stonecraft project. */
export type GeneratedStonecraftProject = {
    filename: string;
    archive: Blob;
};

type TemplateValues = Record<string, string | boolean>;

function render(template: string, values: TemplateValues): string {
    return Mustache.render(template, values, undefined, MUSTACHE_OPTIONS);
}

function entrypointClassFor(modName: string, modId: string): string {
    const words = modName.match(/[A-Za-z0-9]+/g) ?? modId.match(/[A-Za-z0-9]+/g)!;
    const className = words
        .map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1)}`)
        .join('');

    return /^\d/.test(className) ? `Mod${className}` : className;
}

function templateValues(options: GenerateStonecraftProjectOptions): TemplateValues {
    return {
        AUTHOR: options.author,
        BASE_PACKAGE: `${options.group}.${options.modId}`,
        COPYRIGHT_HOLDER: options.author,
        COPYRIGHT_YEAR: String(new Date().getUTCFullYear()),
        ENTRYPOINT_CLASS: entrypointClassFor(options.modName, options.modId),
        MOD_DESCRIPTION: `A Minecraft mod called ${options.modName}.`,
        MOD_GROUP: options.group,
        MOD_ID: options.modId,
        MOD_NAME: options.modName,
        PACKAGE_PATH: `${options.group.replaceAll('.', '/')}/${options.modId}`,
        REPOSITORY: Boolean(options.repository),
        REPOSITORY_URL: options.repository ?? '',
        DATAGEN: options.features.dataGeneration,
        GAMETESTS: options.features.gameTests,
        PUBLISHING: options.features.publishing,
        AUTOMATED_RELEASES: options.features.automatedReleases,
        RENOVATE: options.features.renovate,
    };
}

async function loadTemplate(): Promise<JSZip> {
    const response = await fetch(TEMPLATE_URL);
    if (!response.ok) {
        throw new Error(`Unable to fetch ${TEMPLATE_URL}: HTTP ${response.status}`);
    }

    return JSZip.loadAsync(await response.arrayBuffer());
}

async function renderTemplate(template: JSZip, values: TemplateValues): Promise<Blob> {
    const output = new JSZip();

    for (const entry of Object.values(template.files)) {
        const path = render(entry.name, values);
        const binary = /\.(jar|png)$/.test(entry.name);
        const content = binary
            ? await entry.async('uint8array')
            : render(await entry.async('string'), values);

        if (!binary && content.length === 0) {
            continue;
        }

        output.file(path, content, {
            binary,
            createFolders: false,
            date: entry.date,
            unixPermissions: entry.unixPermissions as number,
        });
    }

    return output.generateAsync({
        type: 'blob',
        mimeType: 'application/zip',
        compression: 'DEFLATE',
        compressionOptions: {level: 9},
        platform: 'UNIX',
    });
}

/** Downloads the template and renders a new Stonecraft project. */
export async function generateStonecraftProject(
    options: GenerateStonecraftProjectOptions,
): Promise<GeneratedStonecraftProject> {
    const archive = await renderTemplate(await loadTemplate(), templateValues(options));

    return {
        filename: `${options.modId}.zip`,
        archive,
    };
}
