import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import {themes} from "prism-react-renderer";

const lightTheme = themes.oceanicNext;
const darkTheme = themes.oceanicNext;

const config: Config = {
    title: 'Stonecraft – Simplify Multi-Loader, Multi-Version Minecraft Mod Development',
    titleDelimiter: '–',
    favicon: 'img/stonecraft@0.25x.png',
    url: 'https://stonecraft.meza.gg',
    baseUrl: '/',
    trailingSlash: false,

    /* Your site config here */

    i18n: {
        defaultLocale: "en",
        locales: ["en"],
    },
    presets: [
        [
            '@docusaurus/preset-classic',
            {
                theme: {
                    customCss: ['./src/css/custom.css']
                },
                docs: {
                    sidebarPath: './sidebars.ts',
                    routeBasePath: '/'
                },
                blog: {
                    path: 'blog',
                    routeBasePath: 'blog',
                    showReadingTime: true,
                },
            } satisfies Preset.Options,
        ],
    ],
    headTags: [
        {
            tagName: 'link',
            attributes: {
                rel: 'canonical',
                href: 'https://stonecraft.meza.gg',
            }
        },
        {
            tagName: 'script',
            attributes: {
                type: 'application/ld+json',
            },
            innerHTML: JSON.stringify({
                "@context": "https://schema.org",
                "@type": "SoftwareApplication",
                "name": "Stonecraft",
                "description": "Stonecraft is a Gradle plugin that simplifies multi-loader, multi-version Minecraft mod development with Fabric, Forge, and NeoForge support.",
                "applicationCategory": "DeveloperTool",
                "operatingSystem": "Cross-platform",
                "url": "https://stonecraft.meza.gg/",
                "author": {
                    "@type": "Person",
                    "name": "Meza"
                }
            }),
        }
    ],
    themeConfig: {
        algolia: {
            appId: '9CN68M6PTQ',
            apiKey: 'c04256a693ddd8deb649fcc774ced671',
            indexName: 'stonecraft-meza',
            insights: true,
            contextualSearch: true
        },
        metadata: [
            {name: 'description', content: 'Stonecraft is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'},
            {name: 'keywords', content: 'Minecraft modding, Gradle plugin, Fabric mod, Forge mod, NeoForge, multi-loader modding, Stonecutter, Architectury, Modrinth, CurseForge'},
            {name: 'og:title', content: 'Stonecraft – Simplify Multi-Loader, Multi-Version Minecraft Mod Development'},
            {name: 'og:description', content: 'Stonecraft is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'},
            {name: 'og:type', content: 'website'},
            {name: 'og:url', content: 'https://stonecraft.meza.gg'},
            {name: 'og:image', content: 'https://stonecraft.meza.gg/img/stonecraft@0.5x.png'},
            {name: 'twitter:card', content: 'summary_large_image'},
            {name: 'twitter:site', content: '@houseofmeza'},
            {name: 'twitter:creator', content: '@houseofmeza'},
            {name: 'twitter:image', content: 'https://stonecraft.meza.gg/img/stonecraft@0.5x.png'},
            {name: 'twitter:title', content: 'Stonecraft – Simplify Multi-Loader, Multi-Version Minecraft Mod Development'},
            {name: 'twitter:description', content: 'Stonecraft is a Gradle plugin that streamlines multi-loader, multi-version Minecraft mod development. Build Fabric, Forge, and NeoForge mods in a single codebase and simplify releases to Modrinth and CurseForge.'}
        ],
        announcementBar: {
            isCloseable: true,
            id: 'star',
            backgroundColor: '#e99b45',
            content: '⭐️ If you like Stonecraft, give it a star on <a target="_blank" rel="noopener noreferrer" href="https://github.com/meza/Stonecraft">GitHub</a>! ⭐️',
        },
        colorMode: {
            defaultMode: 'dark',
            disableSwitch: false,
            respectPrefersColorScheme: true,
        },
        navbar: {
            title: "Stonecraft",
            logo: {
                alt: "Stonecraft Logo",
                src: "img/stonecraft@0.5x.png",
            },
            items:[
                {
                    to: '/blog',
                    label: 'Blog',
                    position: 'right',
                },
                {
                    position: 'right',
                    className: 'github-link',
                    'aria-label': 'GitHub repository',
                    href: 'https://github.com/meza/Stonecraft'
                }
            ]
        },
        footer: {
            style: "dark",
            logo: {
                alt: "Stonecraft Logo",
                src: "https://stonecraft.meza.gg/img/stonecraft@0.25x.png",
            },
            links: [
                {
                    title: "Docs",
                    items: [
                        {
                            to: "/",
                            label: "Documentation",
                        },
                        {
                            to: "/contributing",
                            label: "Contributing to the Documentation"
                        }
                    ],
                },
                {
                    title: "Links",
                    items: [
                        {
                            label: "Discord",
                            href: "https://discord.gg/dvg3tcQCPW",
                        },
                        {
                            label: "Main Website",
                            href: "https://stonecraft.meza.gg/",
                        },
                        {
                            label: "GitHub",
                            href: "https://github.com/meza/Stonecraft",
                        },
                    ],
                },
            ],
            copyright: `Copyright © 2025, under the GPL-3.0 license. Built with Docusaurus.`,
        },
        prism: {
            theme: lightTheme,
            darkTheme: darkTheme,
            additionalLanguages: ["java", "gradle", "toml", "groovy", "kotlin", "json", "json5"],
        }
    } satisfies Preset.ThemeConfig
};

export default config;
