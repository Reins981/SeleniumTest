import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        FlightSearchTest.class,
        FlightFilterTest.class
})
public class FlightTestSuite {
    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static Actions actions;
    protected static List<String> airlinesToSelect;
    protected static String origin;
    protected static String destination;

    @BeforeClass
    public static void setUp() {
        origin = "Berlin";
        destination = "Athens";
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        airlinesToSelect = Arrays.asList("Lufthansa");
        driver.get("https://www.flightnetwork.com/");
        // Wait for some elements to appear first which we assume is condition for a successful page load operation
        wait.until(ExpectedConditions.titleContains("Flight Network"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-testid='fade-in-search-form-wrapper']")));
        // Avoid elements being obscured by popups
        FlightSearchHelper.dismissCookieConsentMessage(driver);
        // Scroll to the top of the page
        FlightSearchHelper.scrollPage(FlightTestSuite.driver, 0);
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
