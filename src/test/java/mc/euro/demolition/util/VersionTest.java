package mc.euro.demolition.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;

/**
 * Unit test for simple App.
 */
public class VersionTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public VersionTest( String testName )
    {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( VersionTest.class );
    }

    public void testGetPluginVersion_Plugin() {
    }

    public void testGetPluginVersion_String() {
    }

    public void testGetServerVersion() {
    }

    public void testGetNmsVersion() {
    }

    public void testGetPlugin() {
    }

    public void testIsEnabled() {
        System.out.println("isEnabled");
        String nullVersion = null;
        Version instance = new Version(nullVersion);
        boolean expected = false;
        boolean result = instance.isEnabled();
        assertEquals(expected, result);
        
        instance = new Version("1.2.5");
        result = instance.isEnabled();
        expected = true;
        assertEquals(expected, result);
    }

    public void testIsCompatible() {
        System.out.println("isCompatible");
        String minVersion = "1.2.3";
        Version instance = new Version("1.2.3-b122");
        boolean expected = true;
        boolean result = instance.isCompatible(minVersion);
        assertEquals(expected, result);
        
        minVersion = "1.7.9";
        instance = new Version("1.2.5");
        expected = false;
        result = instance.isCompatible(minVersion);
        assertEquals(expected, result);
        
        minVersion = "1_7_R4";
        instance = new Version("1_7_R5");
        expected = true;
        result = instance.isCompatible(minVersion);
        assertEquals(expected, result);
    }

    public void testIsSupported() {
        System.out.println("isSupported");
        String maxVersion = "1.8";
        Version instance = new Version("1.7.9-R0.3-SNAPSHOT");
        boolean expected = true;
        boolean result = instance.isSupported(maxVersion);
        assertEquals(expected, result);
        
        maxVersion = "1.8";
        instance = new Version("1.8.1");
        expected = false;
        result = instance.isSupported(maxVersion);
        assertEquals(expected, result);
    }
    
    public void testCompareTo() {
        System.out.println("compareTo");
        Version whichVersion = new Version("1.3.6-b122");
        Version instance = new Version("1.3.6-b122");
        int expected = 0;
        int result = instance.compareTo(whichVersion);
        assertEquals(expected, result);
        
        whichVersion = new Version("1.3.6-b121-SNAPSHOT");
        instance = new Version("1.3.6-b122");
        expected = 1;
        result = instance.compareTo(whichVersion);
        assertEquals(expected, result);
        
        whichVersion = new Version("1.3.6-b123");
        instance = new Version("1.3.6-b122");
        expected = -1;
        result = instance.compareTo(whichVersion);
        assertEquals(expected, result);
        
        whichVersion = new Version("1.9");
        instance = new Version("1.5");
        expected = -4;
        result = instance.compareTo(whichVersion);
        assertEquals(expected, result);
        
        whichVersion = new Version("1.5");
        instance = new Version("1.9");
        expected = 4;
        result = instance.compareTo(whichVersion);
        assertEquals(expected, result);
        
        whichVersion = new Version("1.2.0");
        instance = new Version("1.2");
        result = instance.compareTo(whichVersion);
        expected = 0;
        assertEquals(expected, result);
    }
    


    public void testSetSeparator() throws NoSuchFieldException {
        System.out.println("setSeparator");
        String expected = "[,]";
        Version instance = new Version("1.2,3.4").setSeparator(expected);
        Field field;
        field = Version.class.getDeclaredField("separator");
        field.setAccessible(true);
        String result = null;
        try {
            result = (String) field.get(instance);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(VersionTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IllegalArgumentException");
        } catch (IllegalAccessException ex) {
            Logger.getLogger(VersionTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IllegalAccessException");
        }
        assertEquals(expected, result);
    }

    public void testSearch() {
        System.out.println("search");
        String regex = "\\(Dev(\\d+)\\.(\\d+)\\.(\\d+)\\)";
        Version instance = new Version("2.0 (Dev2.14.94) (Phoenix)");
        boolean expected = true;
        boolean result = instance.search(regex);
        assertEquals(expected, result);
    }

    public void testGetSubVersion() {
        System.out.println("getSubVersion");
        String regex = "\\(Dev(\\d+)\\.(\\d+)\\.(\\d+)\\)";
        Version instance = new Version("2.0 (Dev2.14.94) (Phoenix)");
        Version expected = new Version("(Dev2.14.94)");
        Version result = instance.getSubVersion(regex);
        System.out.println("regex result = " + result.toString());
        assertEquals(expected.toString(), result.toString());
    }

    public void testToString() {
        System.out.println("toString");
        String version = "1.2.5";
        Version instance = new Version(version);
        String expected = version;
        String result = instance.toString();
        assertEquals(expected, result);
    }
    
    public void testSort() {
        System.out.println("Random Versions:");
        List<Version> versions = new ArrayList<Version>();
        for (int j = 0; j < 25; j++) {
            Version temp = newRandomVersion();
            versions.add(temp);
            System.out.println("" + j + " " + temp.toString());
        }
        Collections.sort(versions);
        System.out.println("Sorted Versions: (ascending)");
        for (int k = 0; k < 24; k++) {
            Version ver = versions.get(k);
            Version next = versions.get(k + 1);
            System.out.println("" + k + " " + ver.toString());
            assertTrue(ver.compareTo(next) <= 0);
        }
        Collections.reverse(versions);
        System.out.println("Sorted Versions: (descending)");
        for (int k = 0; k < 24; k++) {
            Version ver = versions.get(k);
            Version next = versions.get(k + 1);
            System.out.println("" + k + " " + ver.toString());
            assertTrue(ver.compareTo(next) >= 0);
        }
        
    }
    
    private Version newRandomVersion() {
        Random random = new Random();
        int length = random.nextInt(5) + 1;
        String[] versionArray = new String[length];
        for (int index = 0; index < length; index = index + 1) {
            int temp = random.nextInt(9);
            versionArray[index] = String.valueOf(temp);
        }
        String version = StringUtils.join(versionArray, '.');
        return new Version(version);
    }
}
