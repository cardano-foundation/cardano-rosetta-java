// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Cardano Rosetta Java',
  tagline: 'A lightweight Java implementation of the Mesh (formerly Rosetta) API for Cardano',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://cardano-foundation.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/cardano-rosetta-java/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'cardano-foundation', // Usually your GitHub org/user name.
  projectName: 'cardano-rosetta-java', // Usually your repo name.
  deploymentBranch: 'gh-pages',  // Branch for GitHub Pages deployment

  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/cardano-foundation/cardano-rosetta-java/tree/main/docs/',
          showLastUpdateAuthor: true,
          showLastUpdateTime: true,
          exclude: [
            '**/test-results/**',  // Exclude all test-results directories
            '**/_test-results/**',  // Exclude directories with underscore prefix
          ],
        },
        blog: false,  // Disable blog functionality
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  // Configure the mermaid theme
  markdown: {
    mermaid: true,
  },
  themes: ['@docusaurus/theme-mermaid'],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      image: 'img/cardano-social-card.jpg',
      navbar: {
        title: 'Cardano Rosetta Java',
        logo: {
          alt: 'Cardano Logo',
          src: 'img/cardano-starburst-blue.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Docs',
          },
          {
            type: 'search',
            position: 'right',
          },
          {
            href: 'https://github.com/cardano-foundation/cardano-rosetta-java',
            className: 'header-github-link',
            'aria-label': 'GitHub repository',
            position: 'right',
          },
        ],
      },
      // Add mermaid configuration to themeConfig
      mermaid: {
        theme: {light: 'neutral', dark: 'dark'},
        options: {
          maxTextSize: 50000,
        },
      },
      footer: {
        links: [
          {
            items: [
              {
                html: `
                  <div class="footer-container">
                    <div class="footer-row">
                      <a href="/cardano-rosetta-java/docs/intro" class="footer-icon-link" aria-label="Documentation">
                        <span class="footer-icon footer-icon-docs"></span>
                      </a>
                      <a href="/cardano-rosetta-java/api" class="footer-icon-link" aria-label="API Reference">
                        <span class="footer-icon footer-icon-api"></span>
                      </a>
                      <a href="https://twitter.com/cardano" class="footer-icon-link" aria-label="Twitter">
                        <span class="footer-icon footer-icon-twitter"></span>
                      </a>
                      <a href="https://github.com/cardano-foundation/cardano-rosetta-java" class="footer-icon-link" aria-label="GitHub">
                        <span class="footer-icon footer-icon-github"></span>
                      </a>
                      <a href="https://cardano.org" class="footer-icon-link" aria-label="Cardano">
                        <span class="footer-icon footer-icon-cardano"></span>
                      </a>
                    </div>
                    <div class="footer-copyright">© ${new Date().getFullYear()} Cardano Stiftung</div>
                  </div>
                `,
              },
            ],
          },
        ],
        copyright: ' ',
      },
      
    }),

  plugins: [
    [
      '@scalar/docusaurus',
      {
        label: 'API Reference',
        route: '/cardano-rosetta-java/api',

        configuration: {
          url: '/cardano-rosetta-java/api.yaml',
          hideDownloadButton: true,
          hideDarkModeToggle: true,
          forceDarkModeState: 'light',
          withDefaultFonts: false,
          hideClientButton: true,
          hideTestRequestButton: true,
          //proxyUrl: 'https://proxy.scalar.com',
          servers: [
            {
              url: 'http://localhost:8082',
              name: 'Local',
            },
          ],
          customCss: `* {
            --scalar-font: 'Helvetica Neue', Helvetica, Arial, sans-serif;
          }
          `


        },
      },
    ],
    [
      require.resolve("docusaurus-plugin-search-local"),
      {
        // Options for the local search plugin
        hashed: true,
        indexDocs: true,
        indexPages: true,
      },
    ],
    function suppressDrawioSvgWarnings() {
      // Store original console methods
      const originalWarn = console.warn;
      const originalError = console.error;

      // Filter function to check if message should be suppressed
      // @ts-ignore
      const shouldSuppress = (args) => {
        const message = args[0]?.toString() || '';
        return message.includes('.drawio.svg') ||
               message.includes('unsupported file type');
      };

      return {
        name: 'suppress-drawio-svg-warnings',
        configureWebpack() {
          // Patch console.warn and console.error
          console.warn = function(...args) {
            if (!shouldSuppress(args)) {
              originalWarn.apply(console, args);
            }
          };

          console.error = function(...args) {
            if (!shouldSuppress(args)) {
              originalError.apply(console, args);
            }
          };

          return {
            ignoreWarnings: [
              (warning) => {
                if (!warning.message) return false;
                return (
                  warning.message.includes('.drawio.svg') ||
                  warning.message.includes('unsupported file type')
                );
              },
            ],
          };
        },
      };
    },
  ],
};

export default config;
