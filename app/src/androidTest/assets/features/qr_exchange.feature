Feature: QR exchange key activity
  Using the QR exchange activity

  Scenario: Press back
    Given I am viewing QRExchange activity
    When I press the back button in QRExchange activity
    Then I should return from the QRExchangeActivity