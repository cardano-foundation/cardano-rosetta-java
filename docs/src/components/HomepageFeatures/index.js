import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Mesh API for Cardano',
    Svg: require('@site/static/img/api-interface-svgrepo-com.svg').default,
    description: (
      <>
        Built to the Coinbase Mesh specification with Cardano-specific 
        extensions for supporting native assets, governance, and stake operations.
      </>
    ),
  },
  {
    title: 'Complete Integration Solution',
    Svg: require('@site/static/img/multiple-defenses-svgrepo-com.svg').default,
    description: (
      <>
        All-in-one package with Cardano node, Submit API, Mesh API, and Yaci-Store indexer 
        with Postgres database—streamlining your Cardano integration workflow.
      </>
    ),
  },
  {
    title: 'Efficient & Lightweight',
    Svg: require('@site/static/img/cloud-acceleration-svgrepo-com.svg').default,
    description: (
      <>
        Powered by Yaci-Store for faster synchronization, reduced resource usage, 
        and lower operational costs, optimized for production-grade performance.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col', styles.featureCard)}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
