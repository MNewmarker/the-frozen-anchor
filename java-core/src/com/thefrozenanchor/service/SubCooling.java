package com.thefrozenanchor.service;
import com.thefrozenanchor.model.Refrigerant;
import java.util.InputMismatchException;
import java.util.Scanner;
/**
 * Interactive CLI service handler for calculating condenser subcooling.
 * Prompts the technician for liquid line temperature measurements (in Fahrenheit or Celsius),
 * delegates the polymorphic subcooling calculation to the active {@link Refrigerant} instance,
 * and displays the resulting temperature differential in both units.
 *
 * @author [Your Name], Retired USCG MKC
 */
public class SubCooling {
    /**
     * Constructs and executes the interactive subcooling calculation workflow for a given refrigerant.
     *
     * @param hvac1 The active {@link Refrigerant} instance (pure or blended) containing resolved saturation data.
     */
    public SubCooling(Refrigerant hvac1) {

        Scanner scnr = new Scanner(System.in);
        int calcChoice;
        int tempUnitChoice;
        double liquidLineTemp = 0.0;
        double subcoolingResult;

        try {
            System.out.println("Would you like to calculate Subcooling?");
            System.out.print("Press 1 for Yes | Press 2 for No: ");
            calcChoice = scnr.nextInt();

            if (calcChoice == 1) {
                System.out.println();
                System.out.println("Reminder: Measure liquid line temp close to the metering device (TXV/ piston) or just before the filter drier for cap tube systems.");
                System.out.print("Press 1 for Fahrenheit or 2 for Celsius: ");
                tempUnitChoice = scnr.nextInt();

                if (tempUnitChoice == 1) {
                    System.out.print("Please enter actual liquid line temperature (F): ");
                    liquidLineTemp = scnr.nextDouble();
                } else if (tempUnitChoice == 2) {
                    System.out.print("Please enter actual liquid line temperature (C): ");
                    double tempC = scnr.nextDouble();
                    liquidLineTemp = (tempC * 1.8) + 32.0;
                } else {
                    System.out.println("[!] Invalid unit choice. Returning to main menu...");
                    return;
                }
                // Polymorphic call: resolves against Bubble Point for blends or Sat Temp for pure refrigerants
                subcoolingResult = hvac1.calculateSubcooling(liquidLineTemp);
                // Note: Temperature differentials (Delta-T) convert from F to C by dividing by 1.8 without subtracting 32
                System.out.printf("Calculated Subcooling: %.1f F (%.1f C)%n", subcoolingResult, subcoolingResult / 1.8);
            }
        }
        catch (InputMismatchException e) {
            System.out.println("[!] Invalid input: Please enter numbers only. Returning to main menu...");
            scnr.nextLine();
        }
    }

}
