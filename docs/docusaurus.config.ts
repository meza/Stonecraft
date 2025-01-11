import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import {themes} from "prism-react-renderer";

const lightTheme = themes.oneLight;
const darkTheme = themes.vsDark;

const config: Config = {
    title: 'My Site',
    favicon: 'img/favicon.ico',
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
                blog: false,
            } satisfies Preset.Options,
        ],
    ],

    themeConfig: {
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
            }
        },
        prism: {
            theme: lightTheme,
            darkTheme: darkTheme,
            additionalLanguages: ["java", "gradle", "toml", "groovy", "kotlin", "javascript", "json", "json5"],
        }
    } satisfies Preset.ThemeConfig
};

export default config;
