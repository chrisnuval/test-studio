Feature: Recorded User Interaction
  Scenario: User interaction playback
    When I click on "Search"
    When I enter 'pliers' in 'Search'
    When I click on "div#filters > form:nth-of-type(2) > div:nth-of-type(1) > button:nth-of-type(2)"
