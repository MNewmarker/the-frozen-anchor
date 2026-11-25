package com.thefrozenanchor.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Represents a blended refrigerant (ASHRAE 400 and 500 series) that exhibits temperature glide
 * (e.g., R-410A, R-404A, R-407C). Tracks distinct Bubble Point and Dew Point saturation
 * temperatures from a three-column TSV dataset and applies thermodynamic best practices
 * by referencing Dew Point for superheat and Bubble Point for subcooling.
 *
 * @author [Michael Newmarker], Retired USCG MKC
 */
public class BlendedRefrigerant extends Refrigerant {
    private double bubbleTempF;
    private double dewTempF;
    private double bubbleTempC;
    private double dewTempC;

    /**
     * Default constructor initializing bubble and dew point temperatures to zero.
     */
    public BlendedRefrigerant() {
        super();
        this.bubbleTempF = 0.0;
        this.dewTempF = 0.0;
        this.bubbleTempC = 0.0;
        this.dewTempC = 0.0;
    }

    /**
     * @return Bubble point saturation temperature in degrees Fahrenheit.
     */
    public double getBubbleTempF() {
        return this.bubbleTempF;
    }

    /**
     * @return Dew point saturation temperature in degrees Fahrenheit.
     */
    public double getDewTempF() {

        return this.dewTempF;
    }

    /**
     * @return Bubble point saturation temperature in degrees Celsius.
     */
    public double getBubbleTempC() {

        return this.bubbleTempC;
    }

    /**
     * @return Dew point saturation temperature in degrees Celsius.
     */
    public double getDewTempC() {

        return this.dewTempC;
    }

    /**
     * Reads the corresponding blended refrigerant TSV file (PSIG | Bubble F | Dew F)
     * and locates the closest thermodynamic match using a minimum-difference search.
     * Updates both Fahrenheit and Celsius values for bubble and dew points.
     *
     * @throws IOException If the refrigerant TSV file cannot be located or read.
     */
    @Override
    public void setPT () throws IOException {
        // Normalize user input (e.g., "r-410a" -> "R410A") to match the resource filename
        String cleanName = this.refrigerantName.replace("-", "").replace(" ", "").toUpperCase();
        String filePath = "src/main/resources/refrigerants/" + cleanName + ".tsv";

        FileInputStream fileStream = new FileInputStream(filePath);
        Scanner fileScanner = new Scanner(fileStream);

        double smallestDiff = Double.MAX_VALUE;
        double bestPsig = 0.0;
        double bestBubbleF = 0.0;
        double bestDewF = 0.0;

        // Scan three-column TSV data (PSIG | Bubble Temp F | Dew Temp F)
        while (fileScanner.hasNextDouble()) {


            double rowPsig = fileScanner.nextDouble();
            double rowBubbleF = fileScanner.nextDouble();
            double rowDewF = fileScanner.nextDouble();
            double currentDiff;

            if (this.lookupByPressure) {
                currentDiff = Math.abs(this.psig - rowPsig);
            } else {
                currentDiff = Math.abs(this.tempFahrenheit - rowBubbleF);
            }

            if (currentDiff < smallestDiff) {
                smallestDiff = currentDiff;
                bestPsig = rowPsig;
                bestBubbleF = rowBubbleF;
                bestDewF = rowDewF;
            }
        }
        fileScanner.close();

        this.psig = bestPsig;
        this.bubbleTempF = bestBubbleF;
        this.dewTempF = bestDewF;
        this.bubbleTempC = convertFtoC(bestBubbleF);
        this.dewTempC = convertFtoC(bestDewF);
        this.tempFahrenheit = bestBubbleF;
        this.tempCelsius = this.bubbleTempC;
    }

    /**
     * Calculates evaporator superheat for a blended refrigerant using the Dew Point.
     * Formula: Suction Line Temperature - Dew Point Saturation Temperature.
     *
     * @param suctionLineTemp Measured suction line temperature in degrees Fahrenheit.
     * @return Calculated superheat in degrees Fahrenheit.
     */
    @Override
    public double calculateSuperheat (double suctionLineTemp) {

        return suctionLineTemp - this.dewTempF;
    }

    /**
     * Calculates condenser subcooling for a blended refrigerant using the Bubble Point.
     * Formula: Bubble Point Saturation Temperature - Liquid Line Temperature.
     *
     * @param liquidLineTemp Measured liquid line temperature in degrees Fahrenheit.
     * @return Calculated subcooling in degrees Fahrenheit.
     */
    @Override
    public double calculateSubcooling(double liquidLineTemp) {

        return this.bubbleTempF - liquidLineTemp;
    }

    /**
     * Overrides the base ASCII table output to display both Bubble Point and Dew Point
     * temperatures in Fahrenheit and Celsius alongside gauge pressure.
     */
    @Override
    public void printFormat() {
        String border = ("+" + "-".repeat(16) + "+" + "-".repeat(14) + "+" + "-".repeat(14) + "+" + "-".repeat(14) + "+" + "-".repeat(14) + "+" + "-".repeat(14) + "+");
        System.out.println(border);
        System.out.println("|  Refrigerant   |    PSIG      |    Bubble F  |    Dew F     |    Bubble C   |    Dew C    |");
        System.out.println(border);
        System.out.printf("| %-14s | %-12.1f | %-12.1f | %-12.1f | %-12.1f | %-12.1f | %n", refrigerantName, psig, bubbleTempF, dewTempF, bubbleTempC, dewTempC);
        System.out.println(border);
    }
}
