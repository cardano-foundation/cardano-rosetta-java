// smoke.js — K6 smoke test for Cardano Rosetta Java API
//
// Validates basic endpoint functionality with minimal load (5 VUs, 2 minutes).
// Smoke tests catch regressions without stressing the system.
//
// Usage via Helm:  make k8s-stress-test
// Usage directly:  k6 run --env TARGET_URL=http://localhost:8082 --env NETWORK=preprod smoke.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const networkOptionsLatency = new Trend('network_options_latency', true);
const networkStatusLatency  = new Trend('network_status_latency', true);

const TARGET_URL = __ENV.TARGET_URL || 'http://localhost:8082';
const NETWORK    = __ENV.NETWORK    || 'preprod';

export const options = {
  stages: [
    { duration: '30s', target: 3 },   // ramp up to 3 VUs
    { duration: '1m',  target: 5 },   // steady 5 VUs
    { duration: '30s', target: 0 },   // ramp down
  ],
  thresholds: {
    http_req_failed:          ['rate<0.05'],   // error rate < 5%
    http_req_duration:        ['p(95)<5000'],  // 95th percentile < 5s
    network_options_latency:  ['p(95)<3000'],
    network_status_latency:   ['p(95)<5000'],
    errors:                   ['rate<0.05'],
  },
};

const HEADERS = { 'Content-Type': 'application/json' };

const networkPayload = JSON.stringify({
  network_identifier: { blockchain: 'cardano', network: NETWORK },
  metadata: {},
});

export default function () {
  // 1. /network/list — lightweight endpoint
  let res = http.post(`${TARGET_URL}/network/list`,
    JSON.stringify({ metadata: {} }),
    { headers: HEADERS, tags: { endpoint: 'network_list' } }
  );
  check(res, {
    '/network/list status 200': (r) => r.status === 200,
    '/network/list has network_identifiers': (r) => {
      try { return Array.isArray(JSON.parse(r.body).network_identifiers); }
      catch { return false; }
    },
  });
  errorRate.add(res.status !== 200);
  sleep(0.5);

  // 2. /network/options
  res = http.post(`${TARGET_URL}/network/options`, networkPayload,
    { headers: HEADERS, tags: { endpoint: 'network_options' } }
  );
  networkOptionsLatency.add(res.timings.duration);
  check(res, {
    '/network/options status 200': (r) => r.status === 200,
    '/network/options has version': (r) => {
      try { return JSON.parse(r.body).version !== undefined; }
      catch { return false; }
    },
  });
  errorRate.add(res.status !== 200);
  sleep(0.5);

  // 3. /network/status
  res = http.post(`${TARGET_URL}/network/status`, networkPayload,
    { headers: HEADERS, tags: { endpoint: 'network_status' } }
  );
  networkStatusLatency.add(res.timings.duration);
  check(res, {
    '/network/status status 200': (r) => r.status === 200,
    '/network/status has current_block': (r) => {
      try { return JSON.parse(r.body).current_block_identifier !== undefined; }
      catch { return false; }
    },
  });
  errorRate.add(res.status !== 200);
  sleep(1);
}
