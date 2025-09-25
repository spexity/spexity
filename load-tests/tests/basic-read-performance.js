import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { config } from '../config/config.js';

// Custom metrics
const readErrorRate = new Rate('read_errors');

export const options = {
  scenarios: {
    light_read_load: {
      executor: 'constant-vus',
      vus: 20,
      duration: '2m',
      tags: { scenario: 'light_load' }
    },
    moderate_read_load: {
      executor: 'constant-vus',
      vus: 100,
      duration: '3m',
      tags: { scenario: 'moderate_load' },
      startTime: '2m'
    },
    heavy_read_load: {
      executor: 'constant-vus',
      vus: 200,
      duration: '2m',
      tags: { scenario: 'heavy_load' },
      startTime: '5m'
    }
  },
  thresholds: {
    ...config.THRESHOLDS,
    'http_req_duration{scenario:light_load}': ['p(95)<300'],
    'http_req_duration{scenario:moderate_load}': ['p(95)<500'],
    'http_req_duration{scenario:heavy_load}': ['p(95)<800'],
    'read_errors': ['rate<0.01'],
  }
};

export default function () {
  // Test homepage data loading - most critical read endpoint
  testHomePage();

  // Test communities listing
  testCommunitiesPage();

  // Test individual community page (if we have community IDs)
  testCommunityPage();

  // Random sleep between 1-3 seconds to simulate user reading time
  sleep(Math.random() * 2 + 1);
}

function testHomePage() {
  const response = http.get(`${config.BASE_URL}/api/web/home`, {
    tags: { endpoint: 'home' }
  });

  const success = check(response, {
    'home page loads successfully': (r) => r.status === 200,
    'home page returns posts': (r) => {
      const data = r.json();
      return data && data.posts && Array.isArray(data.posts);
    },
    'home page response time < 500ms': (r) => r.timings.duration < 500,
    'home page has valid post structure': (r) => {
      const data = r.json();
      if (!data || !data.posts || data.posts.length === 0) return true; // Empty is valid

      const firstPost = data.posts[0];
      return firstPost.id && firstPost.subject && firstPost.createdAt;
    }
  });

  if (!success) {
    readErrorRate.add(1);
    console.error(`Home page test failed: ${response.status} - ${response.body}`);
  } else {
    readErrorRate.add(0);
  }
}

function testCommunitiesPage() {
  const response = http.get(`${config.BASE_URL}/api/web/communities`, {
    tags: { endpoint: 'communities' }
  });

  const success = check(response, {
    'communities page loads successfully': (r) => r.status === 200,
    'communities page returns list': (r) => {
      const data = r.json();
      return data && data.communities && Array.isArray(data.communities);
    },
    'communities page response time < 300ms': (r) => r.timings.duration < 300,
    'communities have valid structure': (r) => {
      const data = r.json();
      if (!data || !data.communities || data.communities.length === 0) return true;

      const firstCommunity = data.communities[0];
      return firstCommunity.id && firstCommunity.name;
    }
  });

  if (!success) {
    readErrorRate.add(1);
    console.error(`Communities page test failed: ${response.status} - ${response.body}`);
  } else {
    readErrorRate.add(0);
  }
}

function testCommunityPage() {
  // First get communities to get a valid ID
  const communitiesResponse = http.get(`${config.BASE_URL}/api/web/communities`);

  if (communitiesResponse.status === 200) {
    const data = communitiesResponse.json();
    if (data && data.communities && data.communities.length > 0) {
      // Test the first community
      const communityId = data.communities[0].id;

      const response = http.get(`${config.BASE_URL}/api/web/communities/${communityId}`, {
        tags: { endpoint: 'community_detail' }
      });

      const success = check(response, {
        'community page loads successfully': (r) => r.status === 200,
        'community page returns data': (r) => {
          const communityData = r.json();
          return communityData && communityData.community && communityData.posts;
        },
        'community page response time < 400ms': (r) => r.timings.duration < 400,
        'community has posts structure': (r) => {
          const communityData = r.json();
          return communityData && Array.isArray(communityData.posts);
        }
      });

      if (!success) {
        readErrorRate.add(1);
        console.error(`Community page test failed: ${response.status} - ${response.body}`);
      } else {
        readErrorRate.add(0);
      }
    }
  }
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'results/basic-read-performance.json': JSON.stringify(data, null, 2),
  };
}

// Simple text summary function
function textSummary(data, options = {}) {
  const indent = options.indent || '';
  const enableColors = options.enableColors || false;

  let summary = '\n' + indent + '📊 Basic Read Performance Test Results\n';
  summary += indent + '=====================================\n\n';

  // Request metrics
  summary += indent + '🌐 HTTP Requests:\n';
  summary += indent + `  Total: ${data.metrics.http_reqs.values.count}\n`;
  summary += indent + `  Rate: ${Math.round(data.metrics.http_reqs.values.rate * 100) / 100} req/s\n\n`;

  // Response time metrics
  summary += indent + '⏱️  Response Times:\n';
  summary += indent + `  Average: ${Math.round(data.metrics.http_req_duration.values.avg)}ms\n`;
  summary += indent + `  95th percentile: ${Math.round(data.metrics.http_req_duration.values['p(95)'])}ms\n`;
  summary += indent + `  99th percentile: ${Math.round(data.metrics.http_req_duration.values['p(99)'])}ms\n\n`;

  // Error rate
  summary += indent + '❌ Error Rate:\n';
  summary += indent + `  Failed requests: ${Math.round(data.metrics.http_req_failed.values.rate * 100)}%\n`;
  summary += indent + `  Read errors: ${Math.round((data.metrics.read_errors?.values.rate || 0) * 100)}%\n\n`;

  return summary;
}