Feature: Recorded User Interaction
  Scenario: User interaction playback
    When I click on "Search"
    When I enter 'hammer' in 'Search'
    When I click on "div#filters > form:nth-of-type(2) > div:nth-of-type(1) > button:nth-of-type(2)"
    Then assert "html:nth-of-type(1) > body:nth-of-type(1) > app-root:nth-of-type(1) > div:nth-of-type(1) > app-overview:nth-of-type(1) > div:nth-of-type(3) > div:nth-of-type(2) > div:nth-of-type(1) > a:nth-of-type(2) > div:nth-of-type(1) > img:nth-of-type(1)" visible
    When I click on "undefined/div[3]/div[2]/div[1]/a[2]/div[1]/img[1]"
