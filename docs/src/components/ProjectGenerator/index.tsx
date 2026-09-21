import React, {useEffect, useState} from 'react';
import {
    generateStonecraftProject,
    type GenerateStonecraftProjectOptions,
} from '../../generator';
import styles from './styles.module.css';

type GenerationResult =
    | {kind: 'success'; filename: string}
    | {kind: 'error'; message: string}
    | null;

const featureParameters = [
    ['datagen', 'dataGeneration'],
    ['gametests', 'gameTests'],
    ['publishing', 'publishing'],
    ['releases', 'automatedReleases'],
    ['renovate', 'renovate'],
] as const;

const loaderParameters = [
    ['fabric', 'fabric'],
    ['forge', 'forge'],
    ['neoforge', 'neoForge'],
] as const;

const emptyProject: GenerateStonecraftProjectOptions = {
    modName: '',
    modId: '',
    group: '',
    author: '',
    repository: '',
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

function deriveModId(modName: string): string {
    return modName
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '_')
        .replace(/^_+|_+$/g, '')
        .slice(0, 64);
}

function readProjectFromUrl(search: string): {
    project: GenerateStonecraftProjectOptions;
    modIdEdited: boolean;
} {
    const parameters = new URLSearchParams(search);
    const modName = parameters.get('modName') ?? '';
    const derivedModId = deriveModId(modName);
    const suppliedModId = parameters.get('modId');
    const modId = suppliedModId ?? derivedModId;
    const suppliedFeatures = parameters.get('features');
    const selectedFeatures = suppliedFeatures !== null
        ? new Set(suppliedFeatures.split(','))
        : new Set(featureParameters.map(([parameter]) => parameter));
    const suppliedLoaders = parameters.get('loaders');
    const selectedLoaders = suppliedLoaders !== null
        ? new Set(suppliedLoaders.split(','))
        : new Set(loaderParameters.map(([parameter]) => parameter));

    return {
        project: {
            modName,
            modId,
            group: parameters.get('group') ?? '',
            author: parameters.get('author') ?? '',
            repository: parameters.get('repository') ?? '',
            loaders: Object.fromEntries(
                loaderParameters.map(([parameter, loader]) => [
                    loader,
                    selectedLoaders.has(parameter),
                ]),
            ) as GenerateStonecraftProjectOptions['loaders'],
            features: Object.fromEntries(
                featureParameters.map(([parameter, feature]) => [
                    feature,
                    selectedFeatures.has(parameter),
                ]),
            ) as GenerateStonecraftProjectOptions['features'],
        },
        modIdEdited: suppliedModId !== null,
    };
}

function writeProjectToUrl(
    project: GenerateStonecraftProjectOptions,
    modIdEdited: boolean,
): void {
    const parameters = new URLSearchParams();

    for (const field of [
        'modName',
        'group',
        'author',
        'repository',
    ] as const) {
        if (project[field]) {
            parameters.set(field, project[field]);
        }
    }
    if (modIdEdited) {
        parameters.set('modId', project.modId);
    }

    parameters.set(
        'loaders',
        loaderParameters
            .filter(([, loader]) => project.loaders[loader])
            .map(([parameter]) => parameter)
            .join(','),
    );

    parameters.set(
        'features',
        featureParameters
            .filter(([, feature]) => project.features[feature])
            .map(([parameter]) => parameter)
            .join(','),
    );

    window.history.replaceState(
        window.history.state,
        '',
        `${window.location.pathname}?${parameters}${window.location.hash}`,
    );
}

function download(filename: string, archive: Blob): void {
    const url = URL.createObjectURL(archive);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
}

export default function ProjectGenerator(): React.JSX.Element {
    const [project, setProject] = useState(emptyProject);
    const [modIdEdited, setModIdEdited] = useState(false);
    const [urlLoaded, setUrlLoaded] = useState(false);
    const [generating, setGenerating] = useState(false);
    const [result, setResult] = useState<GenerationResult>(null);

    useEffect(() => {
        const initialState = readProjectFromUrl(window.location.search);
        setProject(initialState.project);
        setModIdEdited(initialState.modIdEdited);
        setUrlLoaded(true);
    }, []);

    useEffect(() => {
        if (urlLoaded) {
            writeProjectToUrl(project, modIdEdited);
        }
    }, [project, modIdEdited, urlLoaded]);

    function updateFeature(
        feature: keyof GenerateStonecraftProjectOptions['features'],
        checked: boolean,
    ): void {
        setProject((current) => ({
            ...current,
            features: {...current.features, [feature]: checked},
        }));
    }

    function updateLoader(
        loader: keyof GenerateStonecraftProjectOptions['loaders'],
        checked: boolean,
    ): void {
        setProject((current) => ({
            ...current,
            loaders: {...current.loaders, [loader]: checked},
        }));
    }

    function updateField(
        field: Exclude<keyof GenerateStonecraftProjectOptions, 'features' | 'loaders'>,
        value: string,
    ): void {
        setProject((current) => ({...current, [field]: value}));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
        event.preventDefault();
        setGenerating(true);
        setResult(null);

        try {
            const generatedProject = await generateStonecraftProject(project);
            download(generatedProject.filename, generatedProject.archive);
            setResult({kind: 'success', filename: generatedProject.filename});
        } catch (error) {
            setResult({
                kind: 'error',
                message: String(error).replace(/^Error:\s*/, ''),
            });
        } finally {
            setGenerating(false);
        }
    }

    return (
        <main className={styles.page}>
            <div className="container">
                <header className={styles.intro}>
                    <p className={styles.eyebrow}>Stonecraft project generator</p>
                    <h1>Create a Stonecraft project</h1>
                    <p>
                        Enter your project details, choose your loaders, and download a ready-to-build
                        workspace.
                    </p>
                </header>

                <form className={styles.form} onSubmit={handleSubmit} aria-busy={generating}>
                    <div className={styles.field}>
                        <label htmlFor="modName">Mod name</label>
                        <input
                            id="modName"
                            name="modName"
                            required
                            pattern={'[^"$\\\\]+'}
                            aria-describedby="modName-help"
                            value={project.modName}
                            onChange={(event) => {
                                const modName = event.currentTarget.value;
                                setProject((current) => ({
                                    ...current,
                                    modName,
                                    modId: modIdEdited ? current.modId : deriveModId(modName),
                                }));
                            }}
                        />
                        <small id="modName-help">
                            For example, Copper Tools. Do not use quotes, dollar signs, or backslashes.
                        </small>
                    </div>

                    <div className={styles.field}>
                        <label htmlFor="modId">Mod ID</label>
                        <input
                            id="modId"
                            name="modId"
                            value={project.modId}
                            onChange={(event) => {
                                setModIdEdited(true);
                                updateField('modId', event.currentTarget.value);
                            }}
                            required
                            pattern="[a-z][a-z0-9_]{1,63}"
                            aria-describedby="modId-help"
                        />
                        <small id="modId-help">
                            Derived from the mod name. Lowercase letters, numbers, and underscores.
                        </small>
                    </div>

                    <div className={styles.field}>
                        <label htmlFor="group">Group</label>
                        <input
                            id="group"
                            name="group"
                            required
                            pattern="[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+"
                            aria-describedby="group-help"
                            value={project.group}
                            onChange={(event) =>
                                updateField('group', event.currentTarget.value)
                            }
                        />
                        <small id="group-help">A lowercase Java package, such as dev.example.</small>
                    </div>

                    <div className={styles.field}>
                        <label htmlFor="author">Author</label>
                        <input
                            id="author"
                            name="author"
                            required
                            pattern={'[^"$\\\\]+'}
                            maxLength={100}
                            aria-describedby="author-help"
                            value={project.author}
                            onChange={(event) =>
                                updateField('author', event.currentTarget.value)
                            }
                        />
                        <small id="author-help">
                            The name shown in mod metadata and the MIT licence. Do not use quotes,
                            dollar signs, or backslashes.
                        </small>
                    </div>

                    <div className={`${styles.field} ${styles.fullWidth}`}>
                        <label htmlFor="repository">Repository URL <span>(optional)</span></label>
                        <input
                            id="repository"
                            name="repository"
                            type="url"
                            pattern={'[^"$\\\\]+'}
                            maxLength={2048}
                            aria-describedby="repository-help"
                            value={project.repository}
                            onChange={(event) =>
                                updateField('repository', event.currentTarget.value)
                            }
                        />
                        <small id="repository-help">
                            For example, https://codeberg.org/example/copper-tools. Encode quotes,
                            dollar signs, and backslashes.
                        </small>
                    </div>

                    <fieldset className={styles.features}>
                        <legend>Mod loaders</legend>
                        <p>Choose at least one loader. All loaders are selected by default.</p>
                        <div className={styles.featureGrid}>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="fabric"
                                    checked={project.loaders.fabric}
                                    onChange={(event) =>
                                        updateLoader('fabric', event.currentTarget.checked)
                                    }
                                    aria-label="Fabric"
                                />
                                <span><strong>Fabric</strong></span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="forge"
                                    checked={project.loaders.forge}
                                    onChange={(event) =>
                                        updateLoader('forge', event.currentTarget.checked)
                                    }
                                    aria-label="Forge"
                                />
                                <span><strong>Forge</strong></span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="neoForge"
                                    checked={project.loaders.neoForge}
                                    onChange={(event) =>
                                        updateLoader('neoForge', event.currentTarget.checked)
                                    }
                                    aria-label="NeoForge"
                                />
                                <span><strong>NeoForge</strong></span>
                            </label>
                        </div>
                    </fieldset>

                    <fieldset className={styles.features}>
                        <legend>Project features</legend>
                        <p>Choose what the generated workspace includes. Everything is selected by default.</p>
                        <div className={styles.featureGrid}>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="dataGeneration"
                                    checked={project.features.dataGeneration}
                                    onChange={(event) =>
                                        updateFeature('dataGeneration', event.currentTarget.checked)
                                    }
                                    aria-label="Data generation"
                                    aria-describedby="dataGeneration-help"
                                />
                                <span>
                                    <strong>Data generation</strong>
                                    <small id="dataGeneration-help">
                                        Example providers, metadata, scripts, and CI commands.
                                    </small>
                                </span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="gameTests"
                                    checked={project.features.gameTests}
                                    onChange={(event) =>
                                        updateFeature('gameTests', event.currentTarget.checked)
                                    }
                                    aria-label="GameTests"
                                    aria-describedby="gameTests-help"
                                />
                                <span>
                                    <strong>GameTests</strong>
                                    <small id="gameTests-help">
                                        Cross-loader test source, resources, and build commands.
                                    </small>
                                </span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="publishing"
                                    checked={project.features.publishing}
                                    onChange={(event) =>
                                        updateFeature('publishing', event.currentTarget.checked)
                                    }
                                    aria-label="Mod publishing"
                                    aria-describedby="publishing-help"
                                />
                                <span>
                                    <strong>Mod publishing</strong>
                                    <small id="publishing-help">
                                        Modrinth and CurseForge publication configuration.
                                    </small>
                                </span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="automatedReleases"
                                    checked={project.features.automatedReleases}
                                    onChange={(event) =>
                                        updateFeature('automatedReleases', event.currentTarget.checked)
                                    }
                                    aria-label="Automated releases"
                                    aria-describedby="automatedReleases-help"
                                />
                                <span>
                                    <strong>Automated releases</strong>
                                    <small id="automatedReleases-help">
                                        GitHub Actions and semantic-release automation.
                                    </small>
                                </span>
                            </label>
                            <label className={styles.featureOption}>
                                <input
                                    type="checkbox"
                                    name="renovate"
                                    checked={project.features.renovate}
                                    onChange={(event) =>
                                        updateFeature('renovate', event.currentTarget.checked)
                                    }
                                    aria-label="Renovate dependency updates"
                                    aria-describedby="renovate-help"
                                />
                                <span>
                                    <strong>Renovate dependency updates</strong>
                                    <small id="renovate-help">
                                        Repository configuration for automated dependency updates.
                                    </small>
                                </span>
                            </label>
                        </div>
                    </fieldset>

                    <div className={styles.actions}>
                        <p className={styles.privacy}>
                            Everything is generated in your browser. Your project details are included
                            in the page URL, so do not enter confidential values.
                        </p>
                        <button
                            className="button button--primary button--lg"
                            type="submit"
                            disabled={generating}
                        >
                            {generating ? 'Generating project...' : 'Generate project'}
                        </button>
                    </div>

                    {result?.kind === 'success' && (
                        <p className={styles.success} role="status">
                            {result.filename} is ready. Unzip it and follow the README to get started.
                        </p>
                    )}
                    {result?.kind === 'error' && (
                        <p className={styles.error} role="alert">
                            {result.message}
                        </p>
                    )}
                </form>
            </div>
        </main>
    );
}
