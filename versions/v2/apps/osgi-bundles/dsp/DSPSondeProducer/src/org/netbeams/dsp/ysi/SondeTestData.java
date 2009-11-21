package org.netbeams.dsp.ysi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Generates random test data. The number of data must be specified during the creation of an instance of this class.
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com> (Until revision 444)
 * @author Marcello de Sales <marcello.sales@gmail.com> (From revision 444)
 * 
 */
public class SondeTestData {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    private static final DecimalFormat ONE_DECIMAL_FORMATTER = new DecimalFormat("###0.0");
    private static final DecimalFormat TWO_DECIMALS_FORMATTER = new DecimalFormat("###0.00");
    private static final DecimalFormat THREE_DECIMALS_FORMATTER = new DecimalFormat("###0.000");

    private static final int CALENDARS_CACHE_SIZE = 15;
    private static final ArrayList<Calendar> calendarsCache = new ArrayList<Calendar>(CALENDARS_CACHE_SIZE);

    static {
        for (int i = 0; i < CALENDARS_CACHE_SIZE; i++) {
            Calendar cdr = Calendar.getInstance();
            cdr.set(Calendar.DAY_OF_MONTH, (int) (Math.random() * (30 - 1)) + 1);
            cdr.set(Calendar.HOUR_OF_DAY, (int) (Math.random() * 23));
            cdr.set(Calendar.MINUTE, (int) (Math.random() * 59));
            cdr.set(Calendar.SECOND, (int) (Math.random() * 59));
            long val1 = cdr.getTimeInMillis();

            cdr.set(Calendar.HOUR_OF_DAY, (int) (Math.random() * 23));
            cdr.set(Calendar.MINUTE, (int) (Math.random() * 59));
            cdr.set(Calendar.SECOND, (int) (Math.random() * 59));
            long val2 = cdr.getTimeInMillis();

            long randomTS = (long) (Math.random() * (val2 - val1)) + val1;
            cdr.setTime(new Date(randomTS));
            calendarsCache.add(cdr);
        }
    }

    /**
     * Creates a new test data with the given number of sonde data to be in the container.
     * 
     * @param numberOfSondeData is the number of data to be in the container.
     */
    private SondeTestData() {

    }

    private static Calendar getRandomCalendar() {
        int calendarIndex = (int) (Math.random() * (calendarsCache.size() - 1));
        return calendarsCache.get(calendarIndex);
    }

    /**
     * @param numberOfRandomSondeDataTypes is the number of Sonde Data to be in the container.
     * @return a SondeDataContainer with a random list of SondeDataTypes containing the given number of random Sonde
     *         Data Types.
     */
    public static SondeDataContainer generateRandomSondeDataContainer(int numberOfRandomSondeDataTypes) {
        SondeDataContainer container = new SondeDataContainer();
        container.sondeData = new ArrayList<SondeDataType>(numberOfRandomSondeDataTypes);
        container.sondeData.addAll(generateRandomSondeData(numberOfRandomSondeDataTypes));
        return container;
    }

    /**
     * @param numberOfRandomSondeData is the number of random sonde data types to be generated.
     * @return a random list of SondeDataType with the current date and time information.
     */
    public static List<SondeDataType> generateRandomSondeData(int numberOfRandomSondeData) {
        List<SondeDataType> randomSondeData = new ArrayList<SondeDataType>(numberOfRandomSondeData);
        for (int i = 0; i < numberOfRandomSondeData; i++) {
            randomSondeData.add(getRandomSondeData());
        }
        return randomSondeData;
    }

    /**
     * @return a random SondeDataType with the current date and time.
     */
    public static SondeDataType getRandomSondeData() {
        SondeDataType sdt = new SondeDataType();
        Calendar cal = getRandomCalendar();
        sdt.setDateTime(DATE_FORMATTER.format(cal.getTime()), TIME_FORMATTER.format(cal.getTime()));
        sdt.setTemp(Float.valueOf(TWO_DECIMALS_FORMATTER.format(100.69 * Math.random())));
        sdt.setSpCond(Float.valueOf(ONE_DECIMAL_FORMATTER.format(183.0 * Math.random())));
        sdt.setCond(Float.valueOf(ONE_DECIMAL_FORMATTER.format(175.0 * Math.random())));
        sdt.setResist(Float.valueOf(TWO_DECIMALS_FORMATTER.format(5704.66 * Math.random())));
        sdt.setSal(Float.valueOf(TWO_DECIMALS_FORMATTER.format(0.09 * Math.random())));
        sdt.setPress(Float.valueOf(THREE_DECIMALS_FORMATTER.format(1.999 * Math.random())));
        sdt.setDepth(Float.valueOf(THREE_DECIMALS_FORMATTER.format(2.999 * Math.random())));
        sdt.setPH(Float.valueOf(TWO_DECIMALS_FORMATTER.format(8.22 * Math.random())));
        sdt.setPhmV(Float.valueOf(ONE_DECIMAL_FORMATTER.format(-94.00 * Math.random())));
        sdt.setODOSat(Float.valueOf(ONE_DECIMAL_FORMATTER.format(111.70 * Math.random())));
        sdt.setODOConc(Float.valueOf(TWO_DECIMALS_FORMATTER.format(66.63 * Math.random())));
        sdt.setTurbid(Float.valueOf(ONE_DECIMAL_FORMATTER.format(0.30 * Math.random())));
        sdt.setBattery(Float.valueOf(ONE_DECIMAL_FORMATTER.format(10.10 * Math.random())));
        return sdt;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SondeDataContainer container = SondeTestData.generateRandomSondeDataContainer(10000);
        long end = System.currentTimeMillis();

        System.out.println("generated in " + (end - start) + " millis of size: " + container.getSondeData().size());
        System.out.println();

        // Verify that the data can be retrieved (first 3).
        for (int i = 0; i < 3; i++) {
            System.out.print("Date: " + container.getSondeData().get(i).getDateString());
            System.out.print(" Time: " + container.getSondeData().get(i).getTimeString());
            System.out.print(" Temp: " + container.getSondeData().get(i).getTemp());
            System.out.print(" SpCond: " + container.getSondeData().get(i).getSpCond());
            System.out.print(" Cond: " + container.getSondeData().get(i).getCond());
            System.out.print(" Resist: " + container.getSondeData().get(i).getResist());
            System.out.print(" Sal: " + container.getSondeData().get(i).getSal());
            System.out.print(" Press: " + container.getSondeData().get(i).getPress());
            System.out.print(" Depth: " + container.getSondeData().get(i).getDepth());
            System.out.print(" pH: " + container.getSondeData().get(i).getPH());
            System.out.print(" pH mV: " + container.getSondeData().get(i).getPhmV());
            System.out.print(" ODO Sat: " + container.getSondeData().get(i).getODOSat());
            System.out.print(" ODO Cond: " + container.getSondeData().get(i).getODOConc());
            System.out.print(" Turbidity: " + container.getSondeData().get(i).getTurbid());
            System.out.println(" Battery: " + container.getSondeData().get(i).getBattery());
            System.out.println();
        }
        // print the last 3
        for (int i = container.getSondeData().size() - 3; i < container.getSondeData().size(); i++) {
            System.out.print("Date: " + container.getSondeData().get(i).getDateString());
            System.out.print(" Time: " + container.getSondeData().get(i).getTimeString());
            System.out.print(" Temp: " + container.getSondeData().get(i).getTemp());
            System.out.print(" SpCond: " + container.getSondeData().get(i).getSpCond());
            System.out.print(" Cond: " + container.getSondeData().get(i).getCond());
            System.out.print(" Resist: " + container.getSondeData().get(i).getResist());
            System.out.print(" Sal: " + container.getSondeData().get(i).getSal());
            System.out.print(" Press: " + container.getSondeData().get(i).getPress());
            System.out.print(" Depth: " + container.getSondeData().get(i).getDepth());
            System.out.print(" pH: " + container.getSondeData().get(i).getPH());
            System.out.print(" pH mV: " + container.getSondeData().get(i).getPhmV());
            System.out.print(" ODO Sat: " + container.getSondeData().get(i).getODOSat());
            System.out.print(" ODO Cond: " + container.getSondeData().get(i).getODOConc());
            System.out.print(" Turbidity: " + container.getSondeData().get(i).getTurbid());
            System.out.println(" Battery: " + container.getSondeData().get(i).getBattery());
            System.out.println();
        }
    }
}
