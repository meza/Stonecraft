import React from 'react';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {axe} from 'vitest-axe';
import JSZip from 'jszip';
import ProjectGenerator from './index';

const project = {
    modName: 'Copper Tools',
    modId: 'copper_tools',
    group: 'dev.example',
    author: 'Example Author',
    repository: 'https://codeberg.org/example/copper-tools',
};
const latestReleaseUrl = 'https://api.github.com/repos/meza/Stonecraft/releases/latest';

function releaseResponse(): Response {
    return Response.json({tag_name: 'v1.13.0'});
}

async function templateResponse(): Promise<Response> {
    const template = new JSZip();
    template.file(
        '__STONECRAFT_MOD_ID__.txt',
        [
            '__STONECRAFT_MOD_NAME__',
            '__STONECRAFT_MOD_ID__',
            '__STONECRAFT_MOD_GROUP__',
            '__STONECRAFT_MOD_DESCRIPTION__',
            '__STONECRAFT_AUTHOR__',
            'version=__STONECRAFT_VERSION__',
            'fabric=__STONECRAFT_#FABRIC__yes__STONECRAFT_/FABRIC__',
            'forge=__STONECRAFT_#FORGE__yes__STONECRAFT_/FORGE__',
            'neoforge=__STONECRAFT_#NEOFORGE__yes__STONECRAFT_/NEOFORGE__',
            '__STONECRAFT_#REPOSITORY__repository=__STONECRAFT_REPOSITORY_URL____STONECRAFT_/REPOSITORY__',
            'datagen=__STONECRAFT_#DATAGEN__yes__STONECRAFT_/DATAGEN__',
            'gametests=__STONECRAFT_#GAMETESTS__yes__STONECRAFT_/GAMETESTS__',
            'publishing=__STONECRAFT_#PUBLISHING__yes__STONECRAFT_/PUBLISHING__',
            'releases=__STONECRAFT_#AUTOMATED_RELEASES__yes__STONECRAFT_/AUTOMATED_RELEASES__',
            'renovate=__STONECRAFT_#RENOVATE__yes__STONECRAFT_/RENOVATE__',
        ].join('\n'),
    );

    return new Response(await template.generateAsync({type: 'uint8array'}), {status: 200});
}

async function generatorResponse(input: RequestInfo | URL): Promise<Response> {
    return input === latestReleaseUrl ? releaseResponse() : templateResponse();
}

async function fillForm(user: ReturnType<typeof userEvent.setup>): Promise<void> {
    await user.type(screen.getByLabelText('Mod name'), project.modName);
    await user.type(screen.getByLabelText('Group'), project.group);
    await user.type(screen.getByLabelText('Author'), project.author);
    await user.type(screen.getByLabelText('Repository URL (optional)'), project.repository);
}

function readBlob(blob: Blob): Promise<ArrayBuffer> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.addEventListener('load', () => resolve(reader.result as ArrayBuffer));
        reader.addEventListener('error', () => reject(reader.error));
        reader.readAsArrayBuffer(blob);
    });
}

describe('ProjectGenerator', () => {
    const createObjectURL = vi.fn(() => 'blob:stonecraft-project');
    const revokeObjectURL = vi.fn();
    let downloadedFilename = '';
    const click = vi
        .spyOn(HTMLAnchorElement.prototype, 'click')
        .mockImplementation(function (this: HTMLAnchorElement) {
            downloadedFilename = this.download;
        });

    beforeEach(() => {
        window.history.replaceState({}, '', '/generator');
        downloadedFilename = '';
        createObjectURL.mockClear();
        revokeObjectURL.mockClear();
        click.mockClear();
        vi.stubGlobal('URL', {...URL, createObjectURL, revokeObjectURL});
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('is accessible and exposes native identifier constraints', async () => {
        const {container} = render(<ProjectGenerator />);

        expect(
            await axe(container, {rules: {'color-contrast': {enabled: false}}}),
        ).toHaveNoViolations();

        const modId = screen.getByLabelText('Mod ID') as HTMLInputElement;
        const modName = screen.getByLabelText('Mod name') as HTMLInputElement;
        const group = screen.getByLabelText('Group') as HTMLInputElement;
        const author = screen.getByLabelText('Author') as HTMLInputElement;
        const repository = screen.getByLabelText('Repository URL (optional)') as HTMLInputElement;
        expect(screen.queryByLabelText('Description')).toBeNull();

        modName.value = 'Quoted "Mod"';
        modId.value = 'Bad-ID';
        group.value = 'Example';
        author.value = 'Quoted "Author"';
        repository.value = 'https://codeberg.org/example/$project';
        expect(modName.checkValidity()).toBe(false);
        expect(modId.checkValidity()).toBe(false);
        expect(group.checkValidity()).toBe(false);
        expect(author.checkValidity()).toBe(false);
        expect(repository.checkValidity()).toBe(false);

        repository.value = 'not a URL';
        expect(repository.checkValidity()).toBe(false);

        modName.value = project.modName;
        modId.value = project.modId;
        group.value = project.group;
        author.value = project.author;
        repository.value = project.repository;
        expect(modName.checkValidity()).toBe(true);
        expect(modId.checkValidity()).toBe(true);
        expect(group.checkValidity()).toBe(true);
        expect(author.checkValidity()).toBe(true);
        expect(repository.checkValidity()).toBe(true);

        repository.value = '';
        expect(repository.checkValidity()).toBe(true);
    });

    it('derives an editable mod ID from the mod name', async () => {
        const user = userEvent.setup();
        render(<ProjectGenerator />);

        const modName = screen.getByLabelText('Mod name');
        const modId = screen.getByLabelText('Mod ID') as HTMLInputElement;

        await user.type(modName, 'Copper Tools');
        expect(modId.value).toBe('copper_tools');

        await user.clear(modName);
        await user.type(modName, '--Copper---Tools!!');
        expect(modId.value).toBe('copper_tools');

        await user.clear(modName);
        await user.type(modName, 'A'.repeat(70));
        expect(modId.value).toBe('a'.repeat(64));

        await user.clear(modId);
        await user.type(modId, 'copper_tools_plus');
        expect(modId.value).toBe('copper_tools_plus');

        await user.clear(modName);
        await user.type(modName, 'Tin Tools');
        expect(modId.value).toBe('copper_tools_plus');
    });

    it('restores a shared setup and keeps the URL synchronized', async () => {
        window.history.replaceState(
            {},
            '',
            '/generator?modName=Copper+Tools&modId=copper_tools_plus&group=dev.example&description=Legacy+value&githubOwner=legacy-owner&githubRepository=legacy-repository&author=Example+Author&repository=https%3A%2F%2Fcodeberg.org%2Fexample%2Fcopper-tools&loaders=forge,neoforge,unknown&features=datagen,publishing,unknown&ignored=value#generator',
        );
        const user = userEvent.setup();
        render(<ProjectGenerator />);

        await waitFor(() =>
            expect((screen.getByLabelText('Mod name') as HTMLInputElement).value).toBe('Copper Tools'),
        );
        expect((screen.getByLabelText('Mod ID') as HTMLInputElement).value).toBe('copper_tools_plus');
        expect((screen.getByLabelText('Group') as HTMLInputElement).value).toBe('dev.example');
        expect((screen.getByLabelText('Author') as HTMLInputElement).value).toBe('Example Author');
        expect((screen.getByLabelText('Repository URL (optional)') as HTMLInputElement).value).toBe('https://codeberg.org/example/copper-tools');
        expect((screen.getByRole('checkbox', {name: 'Data generation'}) as HTMLInputElement).checked).toBe(true);
        expect((screen.getByRole('checkbox', {name: 'GameTests'}) as HTMLInputElement).checked).toBe(false);
        expect((screen.getByRole('checkbox', {name: 'Mod publishing'}) as HTMLInputElement).checked).toBe(true);
        expect((screen.getByRole('checkbox', {name: 'Automated releases'}) as HTMLInputElement).checked).toBe(false);
        expect((screen.getByRole('checkbox', {name: 'Renovate dependency updates'}) as HTMLInputElement).checked).toBe(false);
        expect((screen.getByRole('checkbox', {name: 'Fabric'}) as HTMLInputElement).checked).toBe(false);
        expect((screen.getByRole('checkbox', {name: 'Forge'}) as HTMLInputElement).checked).toBe(true);
        expect((screen.getByRole('checkbox', {name: 'NeoForge'}) as HTMLInputElement).checked).toBe(true);

        await user.clear(screen.getByLabelText('Mod name'));
        await user.type(screen.getByLabelText('Mod name'), 'Tin Tools');
        await user.click(screen.getByRole('checkbox', {name: 'GameTests'}));
        await user.click(screen.getByRole('checkbox', {name: 'Fabric'}));

        await waitFor(() => {
            const parameters = new URLSearchParams(window.location.search);
            expect(parameters.get('modName')).toBe('Tin Tools');
            expect(parameters.get('modId')).toBe('copper_tools_plus');
            expect(parameters.get('features')).toBe('datagen,gametests,publishing');
            expect(parameters.get('loaders')).toBe('fabric,forge,neoforge');
            expect(parameters.has('description')).toBe(false);
            expect(parameters.has('githubOwner')).toBe(false);
            expect(parameters.has('githubRepository')).toBe(false);
            expect(parameters.has('ignored')).toBe(false);
            expect(window.location.hash).toBe('#generator');
        });
    });

    it('preserves whether a shared Mod ID is automatic or manually controlled', async () => {
        window.history.replaceState(
            {},
            '',
            '/generator?modName=Copper+Tools&modId=copper_tools&features=datagen,unknown',
        );
        const user = userEvent.setup();
        const {unmount} = render(<ProjectGenerator />);

        await waitFor(() =>
            expect((screen.getByLabelText('Mod ID') as HTMLInputElement).value).toBe('copper_tools'),
        );
        await user.clear(screen.getByLabelText('Mod name'));
        await user.type(screen.getByLabelText('Mod name'), 'Tin Tools');
        expect((screen.getByLabelText('Mod ID') as HTMLInputElement).value).toBe('copper_tools');

        unmount();
        window.history.replaceState({}, '', '/generator?modName=Iron+Tools');
        render(<ProjectGenerator />);
        await waitFor(() =>
            expect((screen.getByLabelText('Mod ID') as HTMLInputElement).value).toBe('iron_tools'),
        );
        expect(new URLSearchParams(window.location.search).has('modId')).toBe(false);

        await user.clear(screen.getByLabelText('Mod name'));
        await user.type(screen.getByLabelText('Mod name'), 'Gold Tools');
        expect((screen.getByLabelText('Mod ID') as HTMLInputElement).value).toBe('gold_tools');
    });

    it('offers every project capability by default', () => {
        render(<ProjectGenerator />);

        for (const name of [
            'Fabric',
            'Forge',
            'NeoForge',
            'Data generation',
            'GameTests',
            'Mod publishing',
            'Automated releases',
            'Renovate dependency updates',
        ]) {
            expect((screen.getByRole('checkbox', {name}) as HTMLInputElement).checked).toBe(true);
        }
    });

    it('generates and downloads a project with every form value', async () => {
        const user = userEvent.setup();
        let resolveFetch!: (response: Response) => void;
        const fetchPromise = new Promise<Response>((resolve) => {
            resolveFetch = resolve;
        });
        vi.stubGlobal(
            'fetch',
            vi.fn((input: RequestInfo | URL) =>
                input === latestReleaseUrl ? fetchPromise : templateResponse(),
            ),
        );
        render(<ProjectGenerator />);
        await fillForm(user);

        await user.click(screen.getByRole('button', {name: 'Generate project'}));

        const loadingButton = screen.getByRole('button', {name: 'Generating project...'});
        expect((loadingButton as HTMLButtonElement).disabled).toBe(true);
        resolveFetch(releaseResponse());

        await screen.findByText('copper_tools.zip is ready. Unzip it and follow the README to get started.');
        expect((screen.getByRole('button', {name: 'Generate project'}) as HTMLButtonElement).disabled).toBe(false);
        expect(createObjectURL).toHaveBeenCalledOnce();
        expect(click).toHaveBeenCalledOnce();
        expect(downloadedFilename).toBe('copper_tools.zip');
        expect(revokeObjectURL).toHaveBeenCalledWith('blob:stonecraft-project');

        const archive = createObjectURL.mock.calls[0][0] as Blob;
        const zip = await JSZip.loadAsync(await readBlob(archive));
        expect(Object.keys(zip.files)).toEqual(['copper_tools.txt']);
        await expect(zip.file('copper_tools.txt')!.async('string')).resolves.toBe(
            [
                project.modName,
                project.modId,
                project.group,
                `A Minecraft mod called ${project.modName}.`,
                project.author,
                'version=1.13.0',
                'fabric=yes',
                'forge=yes',
                'neoforge=yes',
                `repository=${project.repository}`,
                'datagen=yes',
                'gametests=yes',
                'publishing=yes',
                'releases=yes',
                'renovate=yes',
            ].join('\n'),
        );
    });

    it('excludes unchecked project capabilities', async () => {
        const user = userEvent.setup();
        vi.stubGlobal('fetch', vi.fn(generatorResponse));
        render(<ProjectGenerator />);
        await fillForm(user);

        for (const name of [
            'Data generation',
            'GameTests',
            'Mod publishing',
            'Automated releases',
            'Renovate dependency updates',
        ]) {
            await user.click(screen.getByRole('checkbox', {name}));
        }
        await waitFor(() =>
            expect(new URLSearchParams(window.location.search).get('features')).toBe(''),
        );
        await user.click(screen.getByRole('button', {name: 'Generate project'}));
        await waitFor(() => expect(createObjectURL).toHaveBeenCalledOnce());

        const archive = createObjectURL.mock.calls[0][0] as Blob;
        const zip = await JSZip.loadAsync(await readBlob(archive));
        await expect(zip.file('copper_tools.txt')!.async('string')).resolves.toBe(
            [
                project.modName,
                project.modId,
                project.group,
                `A Minecraft mod called ${project.modName}.`,
                project.author,
                'version=1.13.0',
                'fabric=yes',
                'forge=yes',
                'neoforge=yes',
                `repository=${project.repository}`,
                'datagen=',
                'gametests=',
                'publishing=',
                'releases=',
                'renovate=',
            ].join('\n'),
        );
    });

    it('generates a project without repository metadata', async () => {
        const user = userEvent.setup();
        vi.stubGlobal('fetch', vi.fn(generatorResponse));
        render(<ProjectGenerator />);

        await user.type(screen.getByLabelText('Mod name'), project.modName);
        await user.type(screen.getByLabelText('Group'), project.group);
        await user.type(screen.getByLabelText('Author'), project.author);
        await user.click(screen.getByRole('button', {name: 'Generate project'}));
        await waitFor(() => expect(createObjectURL).toHaveBeenCalledOnce());

        const archive = createObjectURL.mock.calls[0][0] as Blob;
        const zip = await JSZip.loadAsync(await readBlob(archive));
        await expect(zip.file('copper_tools.txt')!.async('string')).resolves.toBe(
            [
                project.modName,
                project.modId,
                project.group,
                `A Minecraft mod called ${project.modName}.`,
                project.author,
                'version=1.13.0',
                'fabric=yes',
                'forge=yes',
                'neoforge=yes',
                '',
                'datagen=yes',
                'gametests=yes',
                'publishing=yes',
                'releases=yes',
                'renovate=yes',
            ].join('\n'),
        );
    });

    it('reports a generation failure and allows another attempt', async () => {
        const user = userEvent.setup();
        const fetchMock = vi
            .fn<typeof fetch>()
            .mockResolvedValueOnce(new Response(null, {status: 503}))
            .mockResolvedValueOnce(releaseResponse())
            .mockImplementationOnce(templateResponse);
        vi.stubGlobal('fetch', fetchMock);
        render(<ProjectGenerator />);
        await fillForm(user);

        await user.click(screen.getByRole('button', {name: 'Generate project'}));

        const alert = await screen.findByRole('alert');
        expect(alert.textContent).toContain('Unable to fetch latest Stonecraft release: HTTP 503');
        expect((screen.getByRole('button', {name: 'Generate project'}) as HTMLButtonElement).disabled).toBe(false);

        await user.click(screen.getByRole('button', {name: 'Generate project'}));
        await waitFor(() => expect(createObjectURL).toHaveBeenCalledOnce());
        expect(fetchMock).toHaveBeenCalledTimes(3);
    });

    it('requires at least one mod loader', async () => {
        const user = userEvent.setup();
        const fetchMock = vi.fn();
        vi.stubGlobal('fetch', fetchMock);
        render(<ProjectGenerator />);
        await fillForm(user);

        for (const name of ['Fabric', 'Forge', 'NeoForge']) {
            await user.click(screen.getByRole('checkbox', {name}));
        }
        await user.click(screen.getByRole('button', {name: 'Generate project'}));

        expect((await screen.findByRole('alert')).textContent).toContain(
            'Select at least one mod loader',
        );
        expect(fetchMock).not.toHaveBeenCalled();
    });
});
