/*
 * AUTOGENERATED - DON'T EDIT
 * Your edits in this file will be overwritten in the next build!
 * Modify the docusaurus.config.js file at your site's root instead.
 */
export default {
  "title": "My Site",
  "favicon": "img/favicon.ico",
  "url": "https://stonecraft.meza.gg",
  "baseUrl": "/",
  "trailingSlash": false,
  "i18n": {
    "defaultLocale": "en",
    "locales": [
      "en"
    ],
    "path": "i18n",
    "localeConfigs": {}
  },
  "presets": [
    [
      "@docusaurus/preset-classic",
      {
        "theme": {
          "customCss": [
            "./src/css/custom.css"
          ]
        },
        "docs": {
          "sidebarPath": "./sidebars.ts",
          "routeBasePath": "/"
        },
        "blog": false
      }
    ]
  ],
  "themeConfig": {
    "announcementBar": {
      "isCloseable": true,
      "id": "star",
      "backgroundColor": "#e99b45",
      "content": "⭐️ If you like Stonecraft, give it a star on <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://github.com/meza/Stonecraft\">GitHub</a>! ⭐️"
    },
    "colorMode": {
      "defaultMode": "dark",
      "disableSwitch": false,
      "respectPrefersColorScheme": true
    },
    "navbar": {
      "title": "Stonecraft",
      "logo": {
        "alt": "Stonecraft Logo",
        "src": "img/stonecraft@0.5x.png"
      },
      "hideOnScroll": false,
      "items": []
    },
    "prism": {
      "theme": {
        "plain": {
          "backgroundColor": "hsl(230, 1%, 98%)",
          "color": "hsl(230, 8%, 24%)"
        },
        "styles": [
          {
            "types": [
              "comment",
              "prolog",
              "cdata"
            ],
            "style": {
              "color": "hsl(230, 4%, 64%)"
            }
          },
          {
            "types": [
              "doctype",
              "punctuation",
              "entity"
            ],
            "style": {
              "color": "hsl(230, 8%, 24%)"
            }
          },
          {
            "types": [
              "attr-name",
              "class-name",
              "boolean",
              "constant",
              "number",
              "atrule"
            ],
            "style": {
              "color": "hsl(35, 99%, 36%)"
            }
          },
          {
            "types": [
              "keyword"
            ],
            "style": {
              "color": "hsl(301, 63%, 40%)"
            }
          },
          {
            "types": [
              "property",
              "tag",
              "symbol",
              "deleted",
              "important"
            ],
            "style": {
              "color": "hsl(5, 74%, 59%)"
            }
          },
          {
            "types": [
              "selector",
              "string",
              "char",
              "builtin",
              "inserted",
              "regex",
              "attr-value",
              "punctuation"
            ],
            "style": {
              "color": "hsl(119, 34%, 47%)"
            }
          },
          {
            "types": [
              "variable",
              "operator",
              "function"
            ],
            "style": {
              "color": "hsl(221, 87%, 60%)"
            }
          },
          {
            "types": [
              "url"
            ],
            "style": {
              "color": "hsl(198, 99%, 37%)"
            }
          },
          {
            "types": [
              "deleted"
            ],
            "style": {
              "textDecorationLine": "line-through"
            }
          },
          {
            "types": [
              "inserted"
            ],
            "style": {
              "textDecorationLine": "underline"
            }
          },
          {
            "types": [
              "italic"
            ],
            "style": {
              "fontStyle": "italic"
            }
          },
          {
            "types": [
              "important",
              "bold"
            ],
            "style": {
              "fontWeight": "bold"
            }
          },
          {
            "types": [
              "important"
            ],
            "style": {
              "color": "hsl(230, 8%, 24%)"
            }
          }
        ]
      },
      "darkTheme": {
        "plain": {
          "color": "#9CDCFE",
          "backgroundColor": "#1E1E1E"
        },
        "styles": [
          {
            "types": [
              "prolog"
            ],
            "style": {
              "color": "rgb(0, 0, 128)"
            }
          },
          {
            "types": [
              "comment"
            ],
            "style": {
              "color": "rgb(106, 153, 85)"
            }
          },
          {
            "types": [
              "builtin",
              "changed",
              "keyword",
              "interpolation-punctuation"
            ],
            "style": {
              "color": "rgb(86, 156, 214)"
            }
          },
          {
            "types": [
              "number",
              "inserted"
            ],
            "style": {
              "color": "rgb(181, 206, 168)"
            }
          },
          {
            "types": [
              "constant"
            ],
            "style": {
              "color": "rgb(100, 102, 149)"
            }
          },
          {
            "types": [
              "attr-name",
              "variable"
            ],
            "style": {
              "color": "rgb(156, 220, 254)"
            }
          },
          {
            "types": [
              "deleted",
              "string",
              "attr-value",
              "template-punctuation"
            ],
            "style": {
              "color": "rgb(206, 145, 120)"
            }
          },
          {
            "types": [
              "selector"
            ],
            "style": {
              "color": "rgb(215, 186, 125)"
            }
          },
          {
            "types": [
              "tag"
            ],
            "style": {
              "color": "rgb(78, 201, 176)"
            }
          },
          {
            "types": [
              "tag"
            ],
            "languages": [
              "markup"
            ],
            "style": {
              "color": "rgb(86, 156, 214)"
            }
          },
          {
            "types": [
              "punctuation",
              "operator"
            ],
            "style": {
              "color": "rgb(212, 212, 212)"
            }
          },
          {
            "types": [
              "punctuation"
            ],
            "languages": [
              "markup"
            ],
            "style": {
              "color": "#808080"
            }
          },
          {
            "types": [
              "function"
            ],
            "style": {
              "color": "rgb(220, 220, 170)"
            }
          },
          {
            "types": [
              "class-name"
            ],
            "style": {
              "color": "rgb(78, 201, 176)"
            }
          },
          {
            "types": [
              "char"
            ],
            "style": {
              "color": "rgb(209, 105, 105)"
            }
          }
        ]
      },
      "additionalLanguages": [
        "java",
        "gradle",
        "toml",
        "groovy",
        "kotlin",
        "javascript",
        "json",
        "json5"
      ],
      "magicComments": [
        {
          "className": "theme-code-block-highlighted-line",
          "line": "highlight-next-line",
          "block": {
            "start": "highlight-start",
            "end": "highlight-end"
          }
        }
      ]
    },
    "docs": {
      "versionPersistence": "localStorage",
      "sidebar": {
        "hideable": false,
        "autoCollapseCategories": false
      }
    },
    "blog": {
      "sidebar": {
        "groupByYear": true
      }
    },
    "metadata": [],
    "tableOfContents": {
      "minHeadingLevel": 2,
      "maxHeadingLevel": 3
    }
  },
  "baseUrlIssueBanner": true,
  "future": {
    "experimental_faster": {
      "swcJsLoader": false,
      "swcJsMinimizer": false,
      "swcHtmlMinimizer": false,
      "lightningCssMinimizer": false,
      "mdxCrossCompilerCache": false,
      "rspackBundler": false
    },
    "experimental_storage": {
      "type": "localStorage",
      "namespace": false
    },
    "experimental_router": "browser"
  },
  "onBrokenLinks": "throw",
  "onBrokenAnchors": "warn",
  "onBrokenMarkdownLinks": "warn",
  "onDuplicateRoutes": "warn",
  "staticDirectories": [
    "static"
  ],
  "customFields": {},
  "plugins": [],
  "themes": [],
  "scripts": [],
  "headTags": [],
  "stylesheets": [],
  "clientModules": [],
  "tagline": "",
  "titleDelimiter": "|",
  "noIndex": false,
  "markdown": {
    "format": "mdx",
    "mermaid": false,
    "mdx1Compat": {
      "comments": true,
      "admonitions": true,
      "headingIds": true
    },
    "anchors": {
      "maintainCase": false
    }
  }
};
