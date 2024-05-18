import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import com.google.gson.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FlightSearchHelper {

    public static String fetchSuggestions(String searchTerm) throws Exception {
        String url = "https://www.flightnetwork.com/graphql/searchLocations";
        String payload = "{\"operationName\":\"searchLocations\",\"variables\":{\"searchTerm\":\"" + searchTerm + "\"},\"query\":\"query searchLocations($searchTerm: String) {\\n  searchLocations(searchTerm: $searchTerm) {\\n    city\\n    cityCode\\n    continent\\n    continentCode\\n    country\\n    countryCode\\n    iataCode\\n    id\\n    multipleAirports\\n    name\\n    state\\n    type\\n    __typename\\n  }\\n}\\n\"}";

        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        // Set request method
        con.setRequestMethod("POST");
        // Set request headers
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0");
        con.setRequestProperty("Accept", "*/*");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-ETG-GraphQL-Operation", "searchLocations");
        con.setRequestProperty("Sec-Fetch-Dest", "empty");
        con.setRequestProperty("Sec-Fetch-Mode", "cors");
        con.setRequestProperty("Sec-Fetch-Site", "same-origin");

        // Enable CORS (Cross-Origin Resource Sharing)
        con.setDoOutput(true);

        // Send request payload
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }

    public static void prettyPrintJson(String json) {
        // Create a Gson object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Parse the JSON string to JsonElement
        JsonElement jsonElement = JsonParser.parseString(json);

        // Pretty print the JSON
        String prettyJson = gson.toJson(jsonElement);
        System.out.println(prettyJson);
    }

    public static String parseSuggestions(String jsonSuggestions, int index) {
        // Create a Gson object
        Gson gson = new Gson();

        // Deserialize the JSON string to a JsonObject
        JsonObject suggestionsObject = gson.fromJson(jsonSuggestions, JsonObject.class);

        // Navigate to the 'data' object
        JsonObject dataObject = suggestionsObject.getAsJsonObject("data");

        // Get the 'searchLocations' array
        JsonArray searchLocationsArray = dataObject.getAsJsonArray("searchLocations");

        // Check if the index is valid
        if (index >= 0 && index < searchLocationsArray.size()) {
            // Get the nth element from the array
            JsonElement nthElement = searchLocationsArray.get(index);

            // Get the 'name' attribute from the nth element
            JsonObject nthElementObject = nthElement.getAsJsonObject();
            String name = nthElementObject.get("name").getAsString();

            return name;
        } else {
            return null; // Index out of bounds
        }
    }

    // Method to check if the number of flights is greater than 0
    public static boolean isNumberOfFlightsGreaterThanZero(WebDriver driver, boolean filterIsOpen) {
        String cssSelector;
        WebElement flightsElement;
        if (filterIsOpen) {
            cssSelector = "[data-testid='resultPage-filtersContainer'] span.css-odz9bl";
            flightsElement = driver.findElement(By.cssSelector(cssSelector));
        } else {
            // Find the div element
            WebElement divElement = driver.findElement(By.className("css-1xpzjhn"));

            // Find all span elements under the div
            List<WebElement> spanElements = divElement.findElements(By.tagName("span"));

            // Check if there are at least two span elements
            if (spanElements.size() >= 2) {
                // Get the second span element (indexing starts from 0)
                flightsElement = spanElements.get(1);

                // Print the text of the second span element
                System.out.println("Text of the second span element: " + flightsElement.getText());
            } else {
                System.out.println("There are not enough span elements under the div.");
                return false;
            }
        }
        String flightsText = flightsElement.getText();

        String[] parts = flightsText.split(" ");
        int numberOfFlights = 0; // Default value if the number is not found
        for (String part : parts) {
            try {
                numberOfFlights = Integer.parseInt(part);
                break; // Break the loop once the number is found
            } catch (NumberFormatException e) {
                // If the current part is not a valid integer, continue to the next part
                continue;
            }
        }

        // Check if the number of flights is greater than 0
        return numberOfFlights > 0;
    }

    public static boolean selectTravelDatesSameMonth(WebDriver driver, WebDriverWait wait, String type, String day) {
        try {
            // Find the departure date input field and click on it to trigger the calendar widget
            WebElement departureDateInput = driver.findElement(By.id("singleBound." + type + "Date"));
            departureDateInput.click();

            // Wait for the calendar widget to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rdp-row button[name='day']")));

            // JavaScript code to find and click the button for the specified day
            String script = "var buttons = document.querySelectorAll(\"button[name='day']\");" +
                    "var success = false;" +
                    "buttons.forEach(function(button) {" +
                    "   var dateDiv = button.querySelector('.css-1f5dw0i');" +
                    "   if (dateDiv && dateDiv.textContent.trim() == '" + day + "') {" +
                    "       button.click();" +
                    "       success = true;" +
                    "   }" +
                    "});" +
                    "return success;"; // Return success status

            // Execute the JavaScript to select the date
            boolean success = (boolean) ((JavascriptExecutor) driver).executeScript(script);

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of any exception
        }
    }

    public static void enterLocation(WebDriver driver, String id, String location) {
        WebElement originInput = driver.findElement(By.id(id));
        originInput.clear();
        originInput.sendKeys(location);
    }

    public static void selectAirlines(WebDriver driver, List<String> airlineNames) {
        // Find all airline checkboxes
        List<WebElement> airlineCheckboxes = driver.findElements(By.cssSelector("input[id^='airlines-']"));

        for (WebElement checkbox : airlineCheckboxes) {
            String airlineLabel = checkbox.findElement(By.xpath("./parent::div/../label")).getText();
            boolean isSelected = checkbox.isSelected();
            boolean shouldBeSelected = airlineNames.contains(airlineLabel);

            if (isSelected != shouldBeSelected) {
                checkbox.click();
            }
        }
    }

    public static boolean checkSelectedAirlines(WebDriver driver, List<String> airlineNames, boolean filterIsOpen) {
        try {
            // Find all airline sections
            List<WebElement> sections = driver.findElements(By.cssSelector("section[data-testid^='resultPage-resultTrip']"));

            if (sections.isEmpty()) {
                return false;
            }

            for (WebElement section : sections) {
                // Get the airline name from the section
                String airlineName = section.findElement(By.cssSelector("div[class^='css-akpcxl']")).getText();
                // Check if the airline name is in the list
                if (!airlineNames.contains(airlineName)) {
                    System.out.println("Unexpected Airline Name: " + airlineName);
                    return false;
                } else {
                    System.out.println(" Expected Airline Name: " + airlineName);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while checking selected airlines: " + e.getMessage());
            return false;
        }
    }

    public static boolean pressButton(String cssSelector, String button ) {
        // Find the button using its data-testid attribute
        boolean success = false;
        try {
            WebElement element = FlightTestSuite.driver.findElement(By.cssSelector(cssSelector));
            // Click the button
            System.out.println("Click the <" + button + "> button");
            if ("Show more".equals(button)) {
                // Scroll the page to make the button visible before pressing the button
                FlightSearchHelper.scrollPage(FlightTestSuite.driver, 2);
            }
            FlightTestSuite.actions.moveToElement(element).click().perform();
            success = true;
        } catch (Exception e) {
            if ("Filter".equals(button)) {
                System.out.println("Warning: The Filter Menu is already open!");
            }
        }
        return success;
    }

    public static boolean checkResultPageIsFilterViewVisible() {
        try {
            FlightTestSuite.wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='resultPage-filters-text'")));
        } catch (Exception e) {
            System.out.println("Filters are already open!");
            FlightTestSuite.driver.findElement(By.cssSelector("[data-testid='resultPage-filtersContainer']"));
            return true;
        }
        return false;
    }

    public static boolean isToggleFilterButtonVisible() {
        return FlightSearchHelper.pressButton("button[data-testid='resultPage-toggleFiltersButton-button']", "Filter");
    }

    public static void scrollPage(WebDriver driver, int factor) {
        String script = "return window.innerHeight;";
        Long desiredWindowHeight;
        Long windowHeight = (Long) ((JavascriptExecutor) driver).executeScript(script);
        if (factor > 0) {
            desiredWindowHeight = windowHeight / factor;
            script = "window.scrollTo(0, " + desiredWindowHeight + ");";

        } else {
            // Top of the page
            script = "window.scrollTo(0, 0);";
        }
        ((JavascriptExecutor) driver).executeScript(script);

    }

    public static void dismissCookieConsentMessage(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        // Try to find and click the cookie consent button
        try {
            By cookieConsentButtonBanner = By.cssSelector(".etiCookieBanner");
            wait.until(ExpectedConditions.elementToBeClickable(cookieConsentButtonBanner));
            String cssSelector = "[data-testid='cookieBanner-confirmButton']";
            WebElement cookieConsentButton = driver.findElement(By.cssSelector(cssSelector));
            cookieConsentButton.click();
        } catch (Exception e) {
            System.out.println("Cookie consent popup not found or not clickable");
        }
    }

    public static void handleDepartureArrivalRadio(WebDriver driver, String radioToSelect, String direction) {
        String suffix = getDirectionSuffix(direction);

        // Find the departure and arrival radio buttons
        WebElement departureRadioDiv = driver.findElement(By.cssSelector("label[data-testid='resultPage-departureArrivalFilter-departure" + suffix + "-radio']"));
        WebElement arrivalRadioDiv = driver.findElement(By.cssSelector("label[data-testid='resultPage-departureArrivalFilter-arrival" + suffix + "-radio']"));

        // Depending on the parameter, select the appropriate radio button
        if (radioToSelect.equalsIgnoreCase("Departure")) {
            if (!departureRadioDiv.isSelected()) {
                departureRadioDiv.click(); // Select the departure radio button
            }
        } else if (radioToSelect.equalsIgnoreCase("Arrival")) {
            if (!arrivalRadioDiv.isSelected()) {
                arrivalRadioDiv.click(); // Select the arrival radio button
            }
        } else {
            System.out.println("Invalid radio option");
        }
    }

    public static void handleNumberOfStopsRadio(WebDriver driver, String radioToSelect) {
        // Find the number of stops radio buttons
        WebElement directRadioDiv = driver.findElement(By.cssSelector("label[data-testid='MAX_STOPS-direct"));
        WebElement oneStopRadioDiv = driver.findElement(By.cssSelector("label[data-testid='MAX_STOPS-max1"));
        WebElement allRadioDiv = driver.findElement(By.cssSelector("label[data-testid='MAX_STOPS-all"));

        // Depending on the parameter, select the appropriate radio button
        if (radioToSelect.equalsIgnoreCase("Direct")) {
            directRadioDiv.click();
        } else if (radioToSelect.equalsIgnoreCase("OneStop")) {
            oneStopRadioDiv.click();
        }
        else if (radioToSelect.equalsIgnoreCase("All")) {
            allRadioDiv.click();
        } else {
            System.out.println("Invalid radio option");
        }
    }


    public static void handleFlightSearchOptionCheckBox(WebDriver driver, String checkboxToSelect) {
        // Find the departure and arrival checkboxes
        WebElement returnCheckBox = driver.findElement(By.id("returnOption"));
        WebElement oneWayCheckBox = driver.findElement(By.id("oneWayOption"));
        WebElement multiCityCheckBox = driver.findElement(By.id("multiStopOption"));

        // Check the state of the checkboxes
        boolean isReturnChecked = returnCheckBox.isSelected();
        boolean isOneWayChecked = oneWayCheckBox.isSelected();
        boolean isMultiCityChecked = oneWayCheckBox.isSelected();

        // Depending on the parameter, deselect the other checkbox and select the checkbox passed as an argument
        if (checkboxToSelect.equals("returnOption")) {
            if (isOneWayChecked) {
                oneWayCheckBox.click();
            }
            if (isMultiCityChecked) {
                multiCityCheckBox.click();
            }
            if (!isReturnChecked) {
                returnCheckBox.click();
            }
        } else if (checkboxToSelect.equals("oneWayOption")) {
            if (isReturnChecked) {
                returnCheckBox.click();
            }
            if (isMultiCityChecked) {
                multiCityCheckBox.click();
            }
            if (!isOneWayChecked) {
                oneWayCheckBox.click();
            }
        } else if (checkboxToSelect.equals("multiStopOption")) {
            if (isOneWayChecked) {
                oneWayCheckBox.click();
            }
            if (isReturnChecked) {
                returnCheckBox.click();
            }
            if (!isMultiCityChecked) {
                multiCityCheckBox.click();
            }
        }  else {
            System.out.println("Invalid checkbox value");
        }
    }

    public static int setTimeSliderPosition(WebDriver driver, Actions actions, String sliderType, String time, String direction, String handle, int initialPosition) {
        String suffix = getDirectionSuffix(direction);

        // Find the slider element based on the slider type (departure or arrival)
        WebElement slider = driver.findElement(By.cssSelector("[data-testid='resultPage-departureArrivalFilter-" + sliderType + suffix + "-slider']"));

        // Get the slider width
        int sliderWidth = slider.getSize().getWidth();

        // Get the minimum and maximum time values
        WebElement minValueElement = slider.findElement(By.cssSelector(".css-q8818v"));
        WebElement maxValueElement = slider.findElement(By.cssSelector(".css-gbzxm"));
        String minValue = minValueElement.getText();
        String maxValue = maxValueElement.getText();

        // Parse the time strings to hours and minutes
        int minHour = Integer.parseInt(minValue.substring(0, 2));
        int minMinute = Integer.parseInt(minValue.substring(3));
        int maxHour = Integer.parseInt(maxValue.substring(0, 2));
        int maxMinute = Integer.parseInt(maxValue.substring(3));

        // Parse the given time string to hours and minutes
        int targetHour = Integer.parseInt(time.substring(0, 2));
        int targetMinute = Integer.parseInt(time.substring(3));
        // Calculate the total time range in minutes
        int totalMinutesRange = (maxHour - minHour) * 60 + (maxMinute - minMinute);

        // Calculate the target position based on the given time
        int targetTotalMinutes = (targetHour - minHour) * 60 + (targetMinute - minMinute);
        double targetPercentage = (double) targetTotalMinutes / totalMinutesRange;
        int xOffset = (int) (targetPercentage * sliderWidth);

        if (handle.equals("RightHandle")) {
            xOffset = sliderWidth - xOffset;
        }

        // Move the slider handle to the calculated offset
        String handleSelector = handle.equals("LeftHandle") ? "[data-testid='handle-0']" : "[data-testid='handle-1']";
        WebElement sliderHandle = slider.findElement(By.cssSelector(handleSelector));

        // Get the current position of the handle
        int currentPosition = sliderHandle.getLocation().getX();

        // Calculate the new offset based on the current position of the handle
        int relativeDistance = (((currentPosition + xOffset) - currentPosition) - initialPosition);
        if (handle.equals("RightHandle")) {
            relativeDistance = (xOffset - sliderWidth) + currentPosition;
        }

        actions.clickAndHold(sliderHandle).moveByOffset(relativeDistance, 0).release().perform();

        return currentPosition;
    }

    public static int setPriceSliderPosition(WebDriver driver, Actions actions, String price, String handle, int initialPosition) {
        // Find the slider element
        WebElement slider = driver.findElement(By.cssSelector("[data-testid='resultPage-PRICEFilter-content"));

        // Get the slider width
        int sliderWidth = slider.getSize().getWidth();

        // Get the minimum and maximum price values
        WebElement minValueElement = slider.findElement(By.cssSelector(".css-q8818v"));
        WebElement maxValueElement = slider.findElement(By.cssSelector(".css-gbzxm"));
        String minValueText = minValueElement.getText();
        String maxValueText = maxValueElement.getText();

        // Parse the price strings to numerical values
        double minValue = parsePrice(minValueText);
        double maxValue = parsePrice(maxValueText);

        // Parse the given price string to numerical value
        double targetPrice = parsePrice(price);

        // Calculate the total price range
        double totalPriceRange = maxValue - minValue;

        // Calculate the target position based on the given price
        double targetPercentage = (targetPrice - minValue) / totalPriceRange;
        int xOffset = (int) (targetPercentage * sliderWidth);

        if (handle.equals("RightHandle")) {
            xOffset = sliderWidth - xOffset;
        }

        // Move the slider handle to the calculated offset
        String handleSelector = handle.equals("LeftHandle") ? "[data-testid='handle-0']" : "[data-testid='handle-1']";
        WebElement sliderHandle = slider.findElement(By.cssSelector(handleSelector));

        // Get the current position of the handle
        int currentPosition = sliderHandle.getLocation().getX();

        // Calculate the new offset based on the current position of the handle
        int relativeDistance = (((currentPosition + xOffset) - currentPosition) - initialPosition);
        if (handle.equals("RightHandle")) {
            relativeDistance = (xOffset - sliderWidth) + currentPosition;
        }

        actions.clickAndHold(sliderHandle).moveByOffset(relativeDistance, 0).release().perform();

        return currentPosition;
    }

    // Helper method to parse price strings (e.g., "CA$764.84")
    private static double parsePrice(String priceText) {
        // Remove currency symbols and commas, then parse to double
        return Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
    }

    public static String getMaxSliderTimeValue(WebDriver driver, String sliderType, String direction) {
        String suffix = getDirectionSuffix(direction);

        // Find the slider element based on the slider type (departure or arrival)
        WebElement slider = driver.findElement(By.cssSelector("[data-testid='resultPage-departureArrivalFilter-" + sliderType + suffix + "-slider']"));

        WebElement maxValueElement = slider.findElement(By.cssSelector(".css-gbzxm"));
        return maxValueElement.getText();
    }

    public static double getMaxSliderPriceValue(WebDriver driver) {
        // Find the slider element
        WebElement slider = driver.findElement(By.cssSelector("[data-testid='resultPage-PRICEFilter-content"));

        WebElement maxValueElement = slider.findElement(By.cssSelector(".css-gbzxm"));
        double maxValue = parsePrice(maxValueElement.getText());
        return maxValue;
    }

    public static boolean checkCheapestPrice(WebDriver driver, double expectedStartPrice, double expectedEndPrice) {
        WebElement priceElement = driver.findElement(By.cssSelector(".css-11xww0p"));
        double price = parsePrice(priceElement.getText());
        if (expectedStartPrice < price && price < expectedEndPrice) {
            return true;
        }
        return false;
    }

    public static boolean checkDepartureArrivalTimeRange(WebDriver driver, String expectedStartTime, String expectedEndTime, String direction, String type) {
        try {
            // Find all sections
            List<WebElement> sections = driver.findElements(By.cssSelector("section[data-testid^='resultPage-resultTrip']"));

            if (sections.isEmpty()) {
                return false;
            }

            for (WebElement section : sections) {
                // Find all trip segments
                List<WebElement> segments = section.findElements(By.cssSelector("div[data-testid='tripDetails-segment']"));

                if (segments.isEmpty()) {
                    return false;
                }

                // Retrieve the appropriate segment based on the direction
                WebElement segment;
                if (direction.equals("FromTo")) {
                    segment = segments.getFirst(); // First segment
                } else {
                    // Second segment, assuming there are at least two segments if Return flights or Multicity was selected
                    segment = segments.get(1);
                }

                // Get the departure or arrival time from the segment
                String segmentType;
                if (type.equals("Departure")) {
                    segmentType = "origin";
                } else {
                    segmentType = "destination";
                }
                String segmentTime = segment.findElement(By.cssSelector("div[data-testid='trip-segment-" + segmentType + "-time'] span")).getText();

                // Convert times to minutes since midnight
                int departureTimeMinutes = convertTimeToMinutes(segmentTime);
                int expectedStartTimeMinutes = convertTimeToMinutes(expectedStartTime);
                int expectedEndTimeMinutes = convertTimeToMinutes(expectedEndTime);

                // Check if the departure time is within the expected range
                if (departureTimeMinutes < expectedStartTimeMinutes || departureTimeMinutes > expectedEndTimeMinutes) {
                    System.out.println(type + " Time: " + segmentTime + " outside of time range [" + expectedStartTime + "," + expectedEndTime + "]");
                    return false;
                } else {
                    System.out.println(type + " Time: " + segmentTime + " within time range [" + expectedStartTime + "," + expectedEndTime + "]");
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while checking departure time range: " + e.getMessage());
            return false;
        }
    }

    public static boolean checkNonStopFlights(WebDriver driver) {
        try {
            // Find all sections
            List<WebElement> sections = driver.findElements(By.cssSelector("section[data-testid^='resultPage-resultTrip']"));

            if (sections.isEmpty()) {
                return false;
            }

            int sectionIndex = 1;
            for (WebElement section : sections) {
                // Find all trip segments
                List<WebElement> segments = section.findElements(By.cssSelector("div[data-testid='tripDetails-segment']"));

                if (segments.isEmpty()) {
                    return false;
                }

                // Iterate through the appropriate segments
                int segmentIndex = 1;
                for (WebElement segment: segments) {
                    try {
                        WebElement element = segment.findElement(By.cssSelector("[data-testid='searchResults-segment-stops']"));
                        return false;
                    } catch (Exception e) {
                        System.out.println("Section " + String.valueOf(sectionIndex) + ", Segment " + String.valueOf(segmentIndex) + " is a non stop flight");
                    }
                    segmentIndex += 1;
                }
                sectionIndex += 1;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while checking non stop flights: " + e.getMessage());
            return false;
        }
    }

    public static void resetFilters() {
        pressButton("button[data-testid='filtersForm-resetFilters-button']", "Clear");
    }

    private static int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    private static String getDirectionSuffix(String direction) {
        String suffix = direction.equals("FromTo") ? "0" : "1";
        return suffix;
    }

}

