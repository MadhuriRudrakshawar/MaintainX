Feature: Analytics dashboard API

  Background:
    * def TestContext = Java.type('com.tus.maintainx.integration.KarateSpringContext')
    * def seed = TestContext.getInstance().seedBaselineData()
    * url baseUrl
    * def adminToken = TestContext.getInstance().tokenFor(seed.adminUsername)

  Scenario: analytics dashboard aggregates maintenance and element data
    Given path '/api/v1/analytics/dashboard'
    And header Authorization = 'Bearer ' + adminToken
    When method get
    Then status 200
    And match response.maintenanceStatusCounts ==
      """
      {
        APPROVED: 1,
        PENDING: 1,
        REJECTED: 1
      }
      """
    And match response.elementsByType ==
      """
      {
        FIREWALL: 1,
        ROUTER: 1,
        SWITCH: 1
      }
      """
    And match response.elementsByStatus ==
      """
      {
        ACTIVE: 2,
        MAINTENANCE: 1
      }
      """
    And match response.approvedWindowTimeline ==
      """
      [
        {
          title: 'Core Router Upgrade',
          startTime: '2026-03-10T10:15:00',
          endTime: '2026-03-10T12:15:00'
        }
      ]
      """

  Scenario: analytics dashboard requires authentication
    Given path '/api/v1/analytics/dashboard'
    When method get
    Then status 403
