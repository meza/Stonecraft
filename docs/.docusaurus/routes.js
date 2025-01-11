import React from 'react';
import ComponentCreator from '@docusaurus/ComponentCreator';

export default [
  {
    path: '/',
    component: ComponentCreator('/', '2e7'),
    routes: [
      {
        path: '/',
        component: ComponentCreator('/', '20b'),
        routes: [
          {
            path: '/',
            component: ComponentCreator('/', '314'),
            routes: [
              {
                path: '/Quickstart',
                component: ComponentCreator('/Quickstart', 'ece'),
                exact: true,
                sidebar: "tutorialSidebar"
              },
              {
                path: '/',
                component: ComponentCreator('/', '1e5'),
                exact: true,
                sidebar: "tutorialSidebar"
              }
            ]
          }
        ]
      }
    ]
  },
  {
    path: '*',
    component: ComponentCreator('*'),
  },
];
