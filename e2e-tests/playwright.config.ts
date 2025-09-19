import { defineConfig, devices } from "@playwright/test"

const webPort = process.env.CI ? '4173' : '5173'
/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: "./tests",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: "html",
  use: {
    baseURL: `http://localhost:${webPort}`,
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },

    {
      name: "firefox",
      use: { ...devices["Desktop Firefox"] },
    },

    {
      name: "webkit",
      use: { ...devices["Desktop Safari"] },
    },
  ],

  webServer: [
    {
      cwd: "../web",
      command: `npm run ${process.env.CI ? 'preview' : 'dev'}`,
      url: `http://localhost:${webPort}`,
      reuseExistingServer: !process.env.CI,
    },
    {
      cwd: "../backend/server",
      command: "mvn quarkus:run",
      url: "http://localhost:8080/q/health/ready",
      timeout: 180000,
      reuseExistingServer: !process.env.CI,
    },
  ],
})
