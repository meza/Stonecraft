import {properties as modData} from '../../../gradle.properties';
const metadataUrl = 'https://plugins.gradle.org/m2/gg/meza/stonecraft/gg.meza.stonecraft.gradle.plugin/maven-metadata.xml';

const version = async () => await fetchLatestPluginVersion(metadataUrl).then((version) => {
    if (version) {
        return version;
    } else {
        return '1.+';
    }
})

export const versionInfo = `id("gg.meza.stonecraft") version "${await version()}"`


async function fetchLatestPluginVersion(url: string): Promise<string | null> {
    try {
        // Fetch the XML content
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const xmlText = await response.text();

        // Extract the <latest> version tag content
        const latestTagStart = xmlText.indexOf('<latest>');
        const latestTagEnd = xmlText.indexOf('</latest>');

        if (latestTagStart !== -1 && latestTagEnd !== -1) {
            // Extract and return the version string
            const version = xmlText.substring(latestTagStart + 8, latestTagEnd).trim();
            return version;
        } else {
            console.warn('No <latest> tag found in the XML.');
            return null;
        }
    } catch (error) {
        console.error(`Error fetching or parsing XML: ${error}`);
        return null;
    }
}
