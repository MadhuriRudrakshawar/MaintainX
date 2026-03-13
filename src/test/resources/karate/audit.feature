Feature: Audit log API

  Background:
    * def TestContext = Java.type('com.tus.maintainx.integration.KarateSpringContext')
    * def seed = TestContext.getInstance().seedBaselineData()
    * url baseUrl
    * def approverToken = TestContext.getInstance().tokenFor(seed.approverUsername)
    * def engineerToken = TestContext.getInstance().tokenFor(seed.engineerUsername)

  Scenario: approver can retrieve audit logs in descending timestamp order
    Given path '/api/v1/audit-logs'
    And header Authorization = 'Bearer ' + approverToken
    When method get
    Then status 200
    And match response == '#[3]'
    And match response[0].action == 'APPROVED'
    And match response[0].username == 'approver1'
    And match response[1].action == 'CREATED'
    And match response[1].username == 'engineer1'
    And match response[2].entityType == 'NETWORK_ELEMENT'

  Scenario: approver can filter audit logs by entity type and entity id
    Given path '/api/v1/audit-logs/type/maintenance_window', seed.approvedWindowId
    And header Authorization = 'Bearer ' + approverToken
    When method get
    Then status 200
    And match each response == { id: '#number', entityType: 'MAINTENANCE_WINDOW', entityId: '#(seed.approvedWindowId)', action: '#string', username: '#string', roleName: '#string', details: '#string', createdAt: '#string' }
    And match response[*].action contains ['APPROVED', 'CREATED']

  Scenario: engineer is forbidden from reading audit logs
    Given path '/api/v1/audit-logs'
    And header Authorization = 'Bearer ' + engineerToken
    When method get
    Then status 403
