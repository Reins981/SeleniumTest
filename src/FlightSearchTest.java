import org.junit.Test;
import org.openqa.selenium.Keys;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class FlightSearchTest {

    @Test
    public void testSuccessfulFlightSearch() {
        try {
            // Set Flight Search Option
            FlightSearchHelper.handleFlightSearchOptionCheckBox(FlightTestSuite.driver, "returnOption");
            // Fetch suggestions for origin
            String originSearchTerm = FlightTestSuite.origin;
            String originJsonSuggestions = FlightSearchHelper.fetchSuggestions(originSearchTerm);
            assertFalse("Test Failed: Fetch Origin Suggestions are empty.", originJsonSuggestions.isEmpty());

            String originFirstSuggestion = FlightSearchHelper.parseSuggestions(originJsonSuggestions, 2);

            // Enter origin
            FlightSearchHelper.enterLocation(FlightTestSuite.driver, "searchForm-singleBound-origin-input", originFirstSuggestion);
            Thread.sleep(2000);
            // Use keyboard actions to select the appropriate item
            FlightTestSuite.actions.sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).perform();

            // Fetch suggestions for destination
            String destinationSearchTerm = FlightTestSuite.destination;
            String destinationJsonSuggestions = FlightSearchHelper.fetchSuggestions(destinationSearchTerm);
            assertFalse("Test Failed: Fetch Destination Suggestions are empty.", destinationJsonSuggestions.isEmpty());

            String destinationFirstSuggestion = FlightSearchHelper.parseSuggestions(destinationJsonSuggestions, 0);
            // Enter destination
            FlightSearchHelper.enterLocation(FlightTestSuite.driver, "searchForm-singleBound-destination-input", destinationFirstSuggestion);
            Thread.sleep(2000);
            // Use keyboard actions to select the appropriate item
            FlightTestSuite.actions.sendKeys(Keys.ENTER).perform();
            Thread.sleep(2000);

            boolean success = FlightSearchHelper.selectTravelDatesSameMonth(FlightTestSuite.driver, FlightTestSuite.wait, "departure", "23");
            assertTrue("Test Failed: Select departure travel dates failed.", success);

            Thread.sleep(1000);
            success = FlightSearchHelper.selectTravelDatesSameMonth(FlightTestSuite.driver, FlightTestSuite.wait, "return", "30");
            assertTrue("Test Failed: Select return travel dates failed.", success);
            Thread.sleep(1000);

            FlightSearchHelper.pressButton("button[data-testid='searchForm-searchFlights-button']", "Search");

            success = FlightSearchHelper.checkResultPageIsFilterViewVisible();
            // Verify that the page displays available flights matching the search criteria
            boolean searchResultsAvailable;
            if (success) {
                searchResultsAvailable = FlightSearchHelper.isNumberOfFlightsGreaterThanZero(FlightTestSuite.driver, true);
            } else {
                searchResultsAvailable = FlightSearchHelper.isNumberOfFlightsGreaterThanZero(FlightTestSuite.driver, false);
            }
            assertTrue("Test Failed: Number of search results is 0.", searchResultsAvailable);
            System.out.println("Number of Search results is > 0");
        } catch (Exception e) {
            fail("Test Failed: An exception occurred." + e);
        }
    }
}
