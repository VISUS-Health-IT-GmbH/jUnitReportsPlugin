import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;


/** Simple tests to create reports & metadata */
class GeneralTest {
    /** one successful test */
    @Test void testSuccess() {
        Assertions.assertTrue(true);
    }

    /** one skipped test */
    @Test void testIgnored() {
        Assumptions.assumeTrue(false);
    }

    /** one failing test */
    @Test void testFailure() {
        Assertions.assertTrue(false);
    }
}
