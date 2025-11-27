import React, {type ReactNode} from 'react';
import {useBlogPost} from '@docusaurus/plugin-content-blog/client';
import BlogPostItem from '@theme-original/BlogPostItem';
import type BlogPostItemType from '@theme/BlogPostItem';
import type {WrapperProps} from '@docusaurus/types';
import styles from './styles.module.css';

type Props = WrapperProps<typeof BlogPostItemType>;

export default function BlogPostItemWrapper(props: Props): ReactNode {
  const {isBlogPostPage, metadata} = useBlogPost();
  const summary = metadata.description?.trim();

  if (isBlogPostPage || !summary) {
    return <BlogPostItem {...props} />;
  }

  return (
    <BlogPostItem {...props}>
      <p className={styles.summary}>{summary}</p>
    </BlogPostItem>
  );
}
