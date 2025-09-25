export const config = {
  // Target environment
  BASE_URL: 'https://test.spexity.net',

  // Keycloak configuration
  KEYCLOAK: {
    realm: 'https://id.spexity.net/realms/spexity-non-prod',
    clientId: 'spexity-non-prod',
    tokenEndpoint: 'https://id.spexity.net/realms/spexity-non-prod/protocol/openid-connect/token',
    authEndpoint: 'https://id.spexity.net/realms/spexity-non-prod/protocol/openid-connect/auth'
  },

  // Test accounts
  VERIFIED_ACCOUNT: {
    email: 'test1@example.com',
    password: 'test1'
  },

  UNVERIFIED_ACCOUNT: {
    email: 'test2@example.com',
    password: 'test2'
  },

  // Performance thresholds - high above average expectations
  THRESHOLDS: {
    // 95% of requests should be under 500ms
    http_req_duration: ['p(95)<500'],
    // Error rate should be less than 1%
    http_req_failed: ['rate<0.01'],
    // At least 100 requests per second
    http_reqs: ['rate>100']
  },

  // Load test scenarios
  SCENARIOS: {
    LIGHT_LOAD: {
      vus: 10,
      duration: '30s'
    },
    MODERATE_LOAD: {
      vus: 50,
      duration: '2m'
    },
    HEAVY_LOAD: {
      vus: 200,
      duration: '5m'
    },
    STRESS_TEST: {
      stages: [
        { duration: '1m', target: 50 },   // Ramp up
        { duration: '2m', target: 100 },  // Steady load
        { duration: '2m', target: 200 },  // Heavy load
        { duration: '1m', target: 300 },  // Stress
        { duration: '2m', target: 0 }     // Ramp down
      ]
    }
  }
};