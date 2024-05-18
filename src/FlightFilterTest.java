import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class FlightFilterTest {

    @Test
    public void testAll() {
        testA_FilterByAirline();
        testB_FilterByDepartureAndArrivalTimes();
        testC_FilterByNumberOfStops();
        testD_FilterByCheapestPriceRange();
    }

    public void testA_FilterByAirline() {
        try {
            boolean filterAlreadyOpen = false;
            boolean success = FlightSearchHelper.isToggleFilterButtonVisible();
            if (!success) {
                // Extend Airlines filter first
                FlightSearchHelper.pressButton("[data-testid='resultPage-AIRLINESFilter-show-more-button']", "Show more");
                filterAlreadyOpen = true;
                fail("The Filter View is already active in another page context. This test case scenario is not handled yet!. Handling this test case scenario would require clarification of requirements first");
            }

            // Apply airline filter (e.g., "Lufthansa")
            FlightSearchHelper.selectAirlines(FlightTestSuite.driver,FlightTestSuite.airlinesToSelect);
            FlightSearchHelper.scrollPage(FlightTestSuite.driver, 2);
            Thread.sleep(2000);
            // Validate
            success = FlightSearchHelper.checkSelectedAirlines(FlightTestSuite.driver, FlightTestSuite.airlinesToSelect, filterAlreadyOpen);
            assertTrue("Test Failed: checkSelectedAirlines returned unexpected or no results", success);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test Failed: An exception occurred:" + e);
        }
    }

    public void testB_FilterByDepartureAndArrivalTimes() {
        try {
            // Apply Departure filter FromTo
            FlightSearchHelper.handleDepartureArrivalRadio(FlightTestSuite.driver, "Departure", "FromTo");
            Thread.sleep(1000);
            int initialPosition = FlightSearchHelper.setTimeSliderPosition(FlightTestSuite.driver, FlightTestSuite.actions, "departure", "12:00", "FromTo", "LeftHandle", 0);
            Thread.sleep(3000);
            // Validate
            // Define the expected time range (in 24-hour format)
            String expectedStartTime = "12:00";
            String expectedEndTime = FlightSearchHelper.getMaxSliderTimeValue(FlightTestSuite.driver, "departure", "FromTo");

            // Call the function to check if the departure time is within the expected range
            boolean success = FlightSearchHelper.checkDepartureArrivalTimeRange(FlightTestSuite.driver, expectedStartTime, expectedEndTime, "FromTo", "Departure");
            assertTrue("Test Failed: FromTo Departure times are not within the expected range", success);

            // Apply Arrival filter FromTo
            FlightSearchHelper.handleDepartureArrivalRadio(FlightTestSuite.driver, "Arrival", "FromTo");
            Thread.sleep(1000);
            FlightSearchHelper.setTimeSliderPosition(FlightTestSuite.driver, FlightTestSuite.actions, "arrival", "21:30", "FromTo", "LeftHandle", initialPosition);
            Thread.sleep(3000);
            // Validate
            // Define the expected time range (in 24-hour format)
            expectedStartTime = "21:30";
            expectedEndTime = FlightSearchHelper.getMaxSliderTimeValue(FlightTestSuite.driver, "arrival", "FromTo");

            // Call the function to check if the departure time is within the expected range
            success = FlightSearchHelper.checkDepartureArrivalTimeRange(FlightTestSuite.driver, expectedStartTime, expectedEndTime, "FromTo", "Arrival");
            assertTrue("Test Failed: FromTo Arrival times are not within the expected range", success);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test Failed: An exception occurred:" + e);
        }
    }

    public void testC_FilterByNumberOfStops() {
        try {
            FlightSearchHelper.resetFilters();
            FlightSearchHelper.scrollPage(FlightTestSuite.driver, 0);
            Thread.sleep(1000);
            FlightSearchHelper.handleNumberOfStopsRadio(FlightTestSuite.driver, "Direct");
            Thread.sleep(1000);
            FlightSearchHelper.checkNonStopFlights(FlightTestSuite.driver);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test Failed: An exception occurred:" + e);
        }
    }

    public void testD_FilterByCheapestPriceRange() {
        try {
            FlightSearchHelper.setPriceSliderPosition(FlightTestSuite.driver, FlightTestSuite.actions, "800", "LeftHandle", 0);
            Thread.sleep(3000);
            double expectedStartPrice = 800;
            double expectedEndPrice = FlightSearchHelper.getMaxSliderPriceValue(FlightTestSuite.driver);
            boolean success = FlightSearchHelper.checkCheapestPrice(FlightTestSuite.driver, expectedStartPrice, expectedEndPrice);
            assertTrue("Test Failed: Cheapest Price not within range [" + String.valueOf(expectedStartPrice) + "," + String.valueOf(expectedEndPrice) + "]", success);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test Failed: An exception occurred:" + e);
        }
    }
}
