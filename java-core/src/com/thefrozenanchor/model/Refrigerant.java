package com.thefrozenanchor.model;

import java.io.IOException;
/**
 * Abstract base class representing a generic HVACR refrigerant.
 * Encapsulates shared thermodynamic properties (pressure and saturation temperatures),
 * unit conversions, and standardized console table formatting. Subclasses must implement
 * specific Pressure/Temperature (P/T) lookup logic and superheat/subcooling calculations
 * based on whether the refrigerant is a single-component fluid or a zeotropic/azeotropic blend.
 *
 * @author [Michael Newmarker], Retired USCG MKC
 */


public abstract class Refrigerant {
    protected String refrigerantName;
    protected double psig;
    protected double tempFahrenheit;
    protected double tempCelsius;

    /** Flag indicating lookup direction: true for PSIG-to-Temp, false for Temp-to-PSIG. */
    protected boolean lookupByPressure;
    /**
     * Default constructor initializing thermodynamic fields to zeroed baseline values.
     */
    public Refrigerant() {
        this.refrigerantName = "";
        this.psig = 0.0;
        this.tempFahrenheit = 0.0;
        this.tempCelsius = 0.0;
        this.lookupByPressure = true;
    }
    /**
     * Sets the ASHRAE designation or common name of the refrigerant (e.g., "R-410A", "R-134a").
     *
     * @param reeferType The refrigerant identifier string.
     */
    public void setRefrigerant(String reeferType) {
        this.refrigerantName = reeferType;
    }

    /**
     * Sets the gauge pressure (PSIG) and configures the lookup mode to resolve saturation temperature.
     *
     * @param psig Gauge pressure in pounds per square inch (PSIG).
     */
    public void setPsig(double psig) {
        this.psig = psig;
        this.lookupByPressure = true;
    }

    /**
     * Sets the saturation temperature in Fahrenheit, automatically updates Celsius,
     * and configures the lookup mode to resolve saturation pressure.
     *
     * @param tempF Saturation temperature in degrees Fahrenheit.
     */
    public void setTemp(double tempF) {
        this.tempFahrenheit = tempF;
        this.tempCelsius = convertFtoC(tempF);
        this.lookupByPressure = false;
    }
    /**
     * @return The refrigerant identifier name.
     */
    public String getRefrigerantName() {

        return this.refrigerantName;
    }

    /**
     * @return The gauge pressure in PSIG.
     */
    public double getPsig() {

        return this.psig;
    }

    /**
     * @return The saturation temperature in degrees Fahrenheit.
     */
    public double getTempFahrenheit() {

        return this.tempFahrenheit;
    }

    /**
     * @return The saturation temperature in degrees Celsius.
     */
    public double getTempCelsius() {

        return this.tempCelsius;
    }

    /**
     * Converts a temperature value from Fahrenheit to Celsius.
     *
     * @param tempF Temperature in degrees Fahrenheit.
     * @return Equivalent temperature in degrees Celsius.
     */
    public double convertFtoC(double tempF) {
        double tempC = (tempF - 32.0) / 1.8;
        return tempC;
    }

    /**
     * Outputs a formatted ASCII table displaying the refrigerant name, gauge pressure,
     * and saturation temperatures in both Fahrenheit and Celsius.
     */
    public void printFormat() {
        System.out.println("+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(23) + "+");
        System.out.println("|       Refrigerant      |          PSIG          |         Sat F          |          Sat C        |");
        System.out.println("+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(23) + "+");
        System.out.printf("| %-22s | %-22.1f | %-22.1f | %-21.1f |%n", refrigerantName, psig, tempFahrenheit, tempCelsius);
        System.out.println("+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(24) + "+" + "-".repeat(23) + "+");
        System.out.println();
    }
    /**
     * Resolves and populates the missing Pressure or Temperature values from the
     * corresponding refrigerant TSV data table based on {@code lookupByPressure}.
     *
     * @throws IOException If the refrigerant data file cannot be located or read.
     */
    public abstract void setPT() throws IOException;

    /**
     * Calculates system superheat using the suction line temperature and saturation temperature.
     *
     * @param lineTemp Measured suction line temperature in degrees Fahrenheit.
     * @return Calculated superheat in degrees Fahrenheit.
     */
    public abstract double calculateSuperheat(double lineTemp);

    /**
     * Calculates system subcooling using the liquid line temperature and saturation temperature.
     *
     * @param lineTemp Measured liquid line temperature in degrees Fahrenheit.
     * @return Calculated subcooling in degrees Fahrenheit.
     */
    public abstract double calculateSubcooling(double lineTemp);

}
