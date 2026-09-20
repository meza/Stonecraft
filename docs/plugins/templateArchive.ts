import {promises as fs} from 'node:fs';
import path from 'node:path';

import type {LoadContext, Plugin} from '@docusaurus/types';
import JSZip from 'jszip';
import type {Compiler} from 'webpack';

const PLUGIN_NAME = 'stonecraft-template-archive';
const ARCHIVE_PATH = 'generator/template.zip';
const ARCHIVE_DATE = new Date(Date.UTC(1980, 0, 1, 0, 0, 0, 0));

type TemplateFile = {
    absolutePath: string;
    archivePath: string;
};

async function collectTemplateFiles(templateDirectory: string): Promise<TemplateFile[]> {
    const files: TemplateFile[] = [];

    async function visit(directory: string): Promise<void> {
        const entries = await fs.readdir(directory, {withFileTypes: true});
        entries.sort((left, right) =>
            Buffer.compare(Buffer.from(left.name, 'utf8'), Buffer.from(right.name, 'utf8')),
        );

        for (const entry of entries) {
            const absolutePath = path.join(directory, entry.name);
            const archivePath = path
                .relative(templateDirectory, absolutePath)
                .split(path.sep)
                .join('/');

            if (archivePath.split('/').includes('.git')) {
                throw new Error(`The template archive must not contain Git metadata: ${archivePath}`);
            }

            if (entry.isDirectory()) {
                await visit(absolutePath);
            } else if (entry.isFile()) {
                files.push({absolutePath, archivePath});
            } else {
                throw new Error(`Unsupported template entry: ${archivePath}`);
            }
        }
    }

    await visit(templateDirectory);
    return files;
}

function isExecutable(archivePath: string): boolean {
    return archivePath === 'gradlew' || /^scripts\/[^/]+\.sh$/.test(archivePath);
}

export async function createTemplateArchive(templateDirectory: string): Promise<{
    archive: Buffer;
    files: TemplateFile[];
}> {
    const files = await collectTemplateFiles(templateDirectory);

    if (files.length === 0) {
        throw new Error(`The Stonecraft template is empty: ${templateDirectory}`);
    }

    const zip = new JSZip();
    for (const file of files) {
        zip.file(file.archivePath, await fs.readFile(file.absolutePath), {
            binary: true,
            createFolders: false,
            date: ARCHIVE_DATE,
            unixPermissions: isExecutable(file.archivePath) ? '755' : '644',
        });
    }

    return {
        archive: await zip.generateAsync({
            type: 'nodebuffer',
            compression: 'DEFLATE',
            compressionOptions: {level: 9},
            platform: 'UNIX',
        }),
        files,
    };
}

export class TemplateArchiveWebpackPlugin {
    private readonly templateDirectory: string;

    constructor(templateDirectory: string) {
        this.templateDirectory = templateDirectory;
    }

    apply(compiler: Compiler): void {
        compiler.hooks.thisCompilation.tap(PLUGIN_NAME, (compilation) => {
            compilation.contextDependencies.add(this.templateDirectory);

            compilation.hooks.processAssets.tapPromise(
                {
                    name: PLUGIN_NAME,
                    stage: compiler.webpack.Compilation.PROCESS_ASSETS_STAGE_ADDITIONAL,
                },
                async () => {
                    const {archive, files} = await createTemplateArchive(this.templateDirectory);
                    files.forEach((file) => compilation.fileDependencies.add(file.absolutePath));
                    compilation.emitAsset(
                        ARCHIVE_PATH,
                        new compiler.webpack.sources.RawSource(archive),
                    );
                },
            );
        });
    }
}

export function templateArchivePlugin(context: LoadContext): Plugin {
    const templateDirectory = path.resolve(context.siteDir, '..', 'generator', 'template');

    return {
        name: PLUGIN_NAME,
        configureWebpack(_config, isServer) {
            if (isServer) {
                return {};
            }

            return {
                plugins: [new TemplateArchiveWebpackPlugin(templateDirectory)],
            };
        },
    };
}

export default templateArchivePlugin;
