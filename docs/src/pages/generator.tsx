import React from 'react';
import Layout from '@theme/Layout';
import ProjectGenerator from '../components/ProjectGenerator';

export default function GeneratorPage(): React.JSX.Element {
    return (
        <Layout
            title="Project Generator"
            description="Create and download a ready-to-build Stonecraft project in your browser."
        >
            <ProjectGenerator />
        </Layout>
    );
}
