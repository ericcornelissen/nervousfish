Feature: Change contact activity
  Using the activity to change an existing contact

  Scenario: Press the back button after doing nothing
    Given I am viewing the change contact activity
    When I press the change contact back button
    Then I should go to the activity I visited before the change contact activity

  Scenario Outline: Change the name of a contact
    Given I am viewing the change contact activity
    When I select the contact name
    And I type <newname> as new name
    And I press the save contact changes button
    Then I should go to the activity I visited before the change contact activity
    And the contact should be updated

    Examples:
      | newname   |
      | OtherName |
      | Peter     |
      | @&#%$)^(  |

  Scenario Outline: Press the back button after editing the contact
    Given I am viewing the change contact activity
    When I select the contact name
    And I type <newname> as new name
    And I press the change contact back button
    And I verify that I want to dismiss the contact changes
    Then I should go to the activity I visited before the change contact activity

    Examples:
      | newname |
      | Eric    |
      | Peter   |
      | Karel   |

  Scenario: Change a contacts name to an empty string
    Given I am viewing the change contact activity
    When I select the contact name
    And I remove all text from the name
    And I press the save contact changes button
    And I press OK on the change contact error popup
    Then I should stay in the contact activity
