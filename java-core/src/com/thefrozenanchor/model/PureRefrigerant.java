package com.thefrozenanchor.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Represents a single-component (pure) or azeotropic refrigerant with negligible temperature glide
 * (e.g., R-134a, R-22, R-32). Resolves saturation Pressure/Temperature (P/T) values using a single
 * saturation curve from a tab-separated values (.tsv) data table.
 *
 * @author [Your Name], Retired USCG MKC
 */
public class PureRefrigerant extends Refrigerant {
    /**
     * Default constructor initializing base thermodynamic properties.
     */
    public PureRefrigerant() {
        super();
    }

    /**
     * Reads the corresponding refrigerant TSV file and finds the closest matching
     * saturation point using a minimum-difference search algorithm.
     * Normalizes user input (e.g., "r-134a" -> "R134A") to locate the data resource
     * and updates both Fahrenheit and Celsius saturation temperatures.
     *
     * @throws IOException If the refrigerant TSV file cannot be found or read.
     */
    @Override
    public void setPT() throws IOException {
        String cleanName = this.refrigerantName.replace("-", "").replace(" ", "").toUpperCase();
        String filePath = "src/main/resources/refrigerants/" + cleanName + ".tsv";

        FileInputStream fileStream = new FileInputStream(filePath);
        Scanner fileScanner = new Scanner(fileStream);

        double smallestDiff = Double.MAX_VALUE;
        double bestTempF = 0.0;
        double bestPsig = 0.0;

        // Scan two-column TSV data (TempF | PSIG) to find the closest tabular match
        while (fileScanner.hasNextDouble()) {
            double rowTempF = fileScanner.nextDouble();
            double rowPsig = fileScanner.nextDouble();
            double currentDiff;

            if (this.lookupByPressure) {
                currentDiff = Math.abs(this.psig - rowPsig);
            }
            else {
                currentDiff = Math.abs(this.tempFahrenheit - rowTempF);
            }
            if (currentDiff < smallestDiff) {
                smallestDiff = currentDiff;
                bestTempF = rowTempF;
                bestPsig = rowPsig;
            }
        }
        fileScanner.close();
        this.psig = bestPsig;
        this.tempFahrenheit = bestTempF;
        this.tempCelsius = convertFtoC(bestTempF);
    }

    /**
     * Calculates evaporator superheat for a single-component refrigerant.
     * Formula: Suction Line Temperature - Saturation Temperature.
     *
     * @param suctionLineTemp Measured suction line temperature in degrees Fahrenheit.
     * @return Calculated superheat in degrees Fahrenheit.
     */
    @Override
    public double calculateSuperheat (double suctionLineTemp) {
        return suctionLineTemp - this.tempFahrenheit;
    }

    /**
     * Calculates condenser subcooling for a single-component refrigerant.
     * Formula: Saturation Temperature - Liquid Line Temperature.
     *
     * @param liquidLineTemp Measured liquid line temperature in degrees Fahrenheit.
     * @return Calculated subcooling in degrees Fahrenheit.
     */
    @Override
    public double calculateSubcooling (double liquidLineTemp) {
        return this.tempFahrenheit - liquidLineTemp;
    }

}
