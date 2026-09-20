import React from 'react';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import clsx from 'clsx';
import type {BlogPost} from '@docusaurus/plugin-content-blog';
import styles from './index.module.css';

type Feature = {
  title: string;
  description: React.ReactNode;
  link: string;
  linkLabel: string;
};

const features: Feature[] = [
  {
    title: 'One Workspace, Every Loader',
    description:
      'Stonecraft wires Fabric, Forge, and NeoForge builds through Architectury and Stonecutter so you can target multiple loaders without duplicating Gradle logic.',
    link: '/docs',
    linkLabel: 'Explore the overview',
  },
  {
    title: 'Minutes to First Build',
    description:
      'Generate a complete Stonecraft workspace in your browser, then unzip it and start building across supported Minecraft versions and loaders.',
    link: '/generator',
    linkLabel: 'Generate a project',
  },
  {
    title: 'Automate Publishing',
    description:
      'Ship releases to Modrinth and CurseForge with shared changelogs, version metadata, and per-loader artifacts coming from a single Gradle invocation.',
    link: '/docs/releasing-mods',
    linkLabel: 'Learn the release flow',
  },
];

function FeatureCard({title, description, link, linkLabel}: Feature) {
  return (
    <div className={clsx('col col--4', styles.featureCard)}>
      <div className={clsx('card', styles.homeCard)}>
        <div className="card__body">
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
        <div className="card__footer">
          <Link className="button button--link" to={link}>
            {linkLabel}
          </Link>
        </div>
      </div>
    </div>
  );
}

type BlogArchiveData = {
  archive: {
    blogPosts: BlogPost[];
  };
};

// eslint-disable-next-line @typescript-eslint/no-var-requires
const blogArchive = require('@generated/docusaurus-plugin-content-blog/default/p/blog-archive-f05.json') as BlogArchiveData;

function RecentPosts() {
  const posts = (blogArchive.archive?.blogPosts ?? []).map((post) => post.metadata);

  if (!posts.length) {
    return null;
  }

  const latest = posts.slice(0, 3);

  const formatAuthors = (authors: BlogPost['metadata']['authors']) => {
    const labels = authors
      .map((author) => author.name ?? author.title ?? author.url ?? '')
      .filter(Boolean);

    if (!labels.length) {
      return null;
    }

    if (labels.length === 1) {
      return labels[0];
    }

    if (labels.length === 2) {
      return `${labels[0]} and ${labels[1]}`;
    }

    return `${labels.slice(0, -1).join(', ')}, and ${labels.slice(-1)}`;
  };

  const formatReadingTime = (readingTime?: number) => {
    if (!readingTime) {
      return null;
    }

    const minutes = Math.max(1, Math.round(readingTime));
    return `${minutes} min read`;
  };

  const formatDate = (isoDate: string | Date) =>
    new Date(isoDate).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });

  return (
    <section className={styles.postsSection}>
      <div className="container">
        <div className="row">
          <div className="col col--8">
            <h2>Latest from the blog</h2>
            <p>Announcements and insights from the Stonecraft development log.</p>
          </div>
          <div className={clsx('col col--4 text--right', styles.postsHeaderAction)}>
            <Link className="button button--primary button--outline" to="/blog">
              View all posts
            </Link>
          </div>
        </div>
        <div className="row">
          {latest.map((post) => {
            const {permalink, title, description, date, authors, readingTime} = post;
            const authorLine = formatAuthors(authors);
            const readingTimeText = formatReadingTime(readingTime);

            return (
              <div key={permalink} className="col col--4">
                <div className={clsx('card', styles.homeCard)}>
                  <div className="card__body">
                    <h3 className={styles.postTitle}>
                      <Link to={permalink}>{title}</Link>
                    </h3>
                    <p>
                      {description?.trim().length
                        ? description
                        : 'Read the latest update from the Stonecraft dev log.'}
                    </p>
                  </div>
                  <div className="card__footer">
                    <div className={styles.postMeta}>
                      {authorLine && <span className={styles.postMetaItem}>By {authorLine}</span>}
                      <span className={styles.postMetaItem}>{formatDate(date)}</span>
                      {readingTimeText && (
                        <span className={styles.postMetaItem}>{readingTimeText}</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

export default function Home(): React.JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout description={siteConfig.tagline}>
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <p className={styles.tagline}>Stonecraft</p>
          <h1 className="hero__title">
            Simplify Multi-Loader, Multi-Version Minecraft Development
          </h1>
          <p className="hero__subtitle">
            Configure Fabric, Forge, and NeoForge projects in one Gradle build, automate pack metadata,
            and publish to every platform without duplicating effort.
          </p>
          <div className={styles.heroButtons}>
            <Link className="button button--primary button--lg" to="/generator">
              Generate a project
            </Link>
            <Link className="button button--outline button--lg" to="https://github.com/meza/Stonecraft">
              View on GitHub
            </Link>
          </div>
        </div>
      </header>
      <main>
        <section className={styles.features}>
          <div className="container">
            <div className="row">
              {features.map((feature) => (
                <FeatureCard key={feature.title} {...feature} />
              ))}
            </div>
          </div>
        </section>
        <RecentPosts />
        <section className={styles.ctaSection}>
          <div className="container">
            <div className="row">
              <div className="col col--7">
                <h2>Start a Stonecraft project</h2>
                <p>
                  Create a complete Fabric and NeoForge workspace locally in your browser. Download the ZIP,
                  unzip it, and follow its README.
                </p>
              </div>
              <div className="col col--5 text--right">
                <Link className="button button--secondary button--lg" to="/generator">
                  Generate a project
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
