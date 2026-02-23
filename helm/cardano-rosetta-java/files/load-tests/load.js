// load.js — K6 load test for Cardano Rosetta Java API
//
// Simulates sustained production traffic across core Rosetta endpoints.
// Ramps up to 50 VUs over 2 minutes, holds for 5 minutes, then ramps down.
//
// Usage via Helm:  make k8s-stress-test-load
// Usage directly:  k6 run --env TARGET_URL=http://localhost:8082 --env NETWORK=mainnet load.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate           = new Rate('errors');
const networkListLatency  = new Trend('network_list_latency', true);
const networkStatusLatency = new Trend('network_status_latency', true);
const networkOptionsLatency = new Trend('network_options_latency', true);
const totalRequests       = new Counter('total_requests');

const TARGET_URL = __ENV.TARGET_URL || 'http://localhost:8082';
const NETWORK    = __ENV.NETWORK    || 'mainnet';

export const options = {
  stages: [
    { duration: '2m',  target: 20 },   // ramp up
    { duration: '5m',  target: 50 },   // peak load
    { duration: '2m',  target: 20 },   // step down
    { duration: '1m',  target: 0 },    // cooldown
  ],
  thresholds: {
    http_req_failed:          ['rate<0.02'],   // <2% errors under load
    http_req_duration:        ['p(95)<8000'],  // 95th percentile < 8s
    network_list_latency:     ['p(95)<3000'],
    network_status_latency:   ['p(95)<8000'],
    network_options_latency:  ['p(95)<5000'],
    errors:                   ['rate<0.02'],
  },
};

const HEADERS = { 'Content-Type': 'application/json' };

const networkPayload = JSON.stringify({
  network_identifier: { blockchain: 'cardano', network: NETWORK },
  metadata: {},
});

const listPayload = JSON.stringify({ metadata: {} });

export default function () {
  const scenario = Math.random();
  let res;

  if (scenario < 0.4) {
    // 40%: /network/list — lightest, most cacheable
    res = http.post(`${TARGET_URL}/network/list`, listPayload,
      { headers: HEADERS, tags: { endpoint: 'network_list' } }
    );
    networkListLatency.add(res.timings.duration);
    check(res, { '/network/list 200': (r) => r.status === 200 });

  } else if (scenario < 0.7) {
    // 30%: /network/status — moderate weight
    res = http.post(`${TARGET_URL}/network/status`, networkPayload,
      { headers: HEADERS, tags: { endpoint: 'network_status' } }
    );
    networkStatusLatency.add(res.timings.duration);
    check(res, {
      '/network/status 200': (r) => r.status === 200,
      '/network/status has block': (r) => {
        try { return JSON.parse(r.body).current_block_identifier !== undefined; }
        catch { return false; }
      },
    });

  } else {
    // 30%: /network/options
    res = http.post(`${TARGET_URL}/network/options`, networkPayload,
      { headers: HEADERS, tags: { endpoint: 'network_options' } }
    );
    networkOptionsLatency.add(res.timings.duration);
    check(res, { '/network/options 200': (r) => r.status === 200 });
  }

  errorRate.add(res.status < 200 || res.status >= 300);
  totalRequests.add(1);

  // Random think time between 0.5s and 2.5s to simulate real users
  sleep(Math.random() * 2 + 0.5);
}

export function handleSummary(data) {
  return {
    stdout: `
=== Load Test Summary ===
Total requests:   ${data.metrics.total_requests ? data.metrics.total_requests.values.count : 'N/A'}
Error rate:       ${((data.metrics.errors ? data.metrics.errors.values.rate : 0) * 100).toFixed(2)}%
p95 duration:     ${data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'].toFixed(0) : 'N/A'}ms
`,
  };
}
