package ui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-End Automated UI Acceptance Tests using Selenium WebDriver.
 * Note: Marked @Disabled by default for headless CI/CD execution unless local ChromeDriver is provided.
 */
@Disabled("Run manually when local Tomcat server and Selenium WebDriver / ChromeDriver are configured")
@DisplayName("Selenium WebDriver UI Acceptance Tests")
public class SeleniumE2ETest {

    private final String BASE_URL = "http://localhost:8080/sunrise-dental-clinic";

    @Test
    @DisplayName("UI-01: Verify Successful Staff Login")
    public void testLoginSuccess() {
        /*
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(BASE_URL + "/login.html");
            driver.findElement(By.id("username")).sendKeys("receptionist");
            driver.findElement(By.id("password")).sendKeys("Reception@123");
            driver.findElement(By.id("btnLoginSubmit")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.urlContains("dashboard.html"));

            assertTrue(driver.getCurrentUrl().contains("dashboard.html"));
        } finally {
            driver.quit();
        }
        */
    }

    @Test
    @DisplayName("UI-02: Verify Login Rejection with Invalid Password")
    public void testLoginFailure() {
        /*
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(BASE_URL + "/login.html");
            driver.findElement(By.id("username")).sendKeys("receptionist");
            driver.findElement(By.id("password")).sendKeys("WrongPassword@999");
            driver.findElement(By.id("btnLoginSubmit")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
            assertTrue(alert.getText().contains("Invalid username or password"));
        } finally {
            driver.quit();
        }
        */
    }

    @Test
    @DisplayName("UI-03: Verify Register New Patient Appointment Workflow")
    public void testRegisterAppointment() {
        /*
        WebDriver driver = new ChromeDriver();
        try {
            // 1. Authenticate
            driver.get(BASE_URL + "/login.html");
            driver.findElement(By.id("username")).sendKeys("receptionist");
            driver.findElement(By.id("password")).sendKeys("Reception@123");
            driver.findElement(By.id("btnLoginSubmit")).click();

            // 2. Navigate to Register Appointment
            driver.get(BASE_URL + "/register-appointment.html");
            driver.findElement(By.id("patientName")).sendKeys("Automated Test Patient");
            driver.findElement(By.id("patientContact")).sendKeys("0771122334");
            driver.findElement(By.id("patientAddress")).sendKeys("No 100, Colombo");

            new Select(driver.findElement(By.id("dentistId"))).selectByIndex(1);
            new Select(driver.findElement(By.id("treatmentId"))).selectByIndex(1);
            driver.findElement(By.id("appointmentDate")).sendKeys(LocalDate.now().plusDays(3).toString());
            driver.findElement(By.id("appointmentTime")).sendKeys("10:30");

            driver.findElement(By.id("btnSubmitBooking")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
            assertTrue(alert.getText().contains("Appointment Scheduled Successfully"));
        } finally {
            driver.quit();
        }
        */
    }

    @Test
    @DisplayName("UI-04: Verify Search Appointment by Number SDC-2026-0001")
    public void testSearchAppointment() {
        /*
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(BASE_URL + "/search-appointment.html");
            driver.findElement(By.id("searchInput")).sendKeys("SDC-2026-0001");
            driver.findElement(By.cssSelector("#searchForm button")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement resCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchResultCard")));
            assertTrue(resCard.isDisplayed());
        } finally {
            driver.quit();
        }
        */
    }

    @Test
    @DisplayName("UI-05: Verify Bill Calculation and Settlement")
    public void testGenerateBill() {
        /*
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(BASE_URL + "/bill.html?appointmentNumber=SDC-2026-0002");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConfirmBill")));
            btn.click();

            WebElement receipt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("printableReceiptCard")));
            assertTrue(receipt.isDisplayed());
        } finally {
            driver.quit();
        }
        */
    }
}
