package net.spexity

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
@QuarkusTestResource(DatabaseTestContainersManager::class)
class GreetingResourceIT : GreetingResourceTest()
