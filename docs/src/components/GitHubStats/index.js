import React from 'react';
import styles from './styles.module.css';

export default function GitHubStats() {
  return (
    <section className={styles.stats}>
      <div className="container">
        <div className="row">
          <div className="col">
            <h2>Project Stats</h2>
            <div className={styles.statGrid}>
              <div className={styles.statItem}>
                <img src="https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=alert_status" alt="Quality Gate" />
              </div>
              <div className={styles.statItem}>
                <img src="https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=coverage" alt="Coverage" />
              </div>
              <div className={styles.statItem}>
                <img src="https://app.fossa.com/api/projects/custom%2B45571%2Fgithub.com%2Fcardano-foundation%2Fcardano-rosetta-java.svg?type=shield&issueType=license" alt="FOSSA Status" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
} 