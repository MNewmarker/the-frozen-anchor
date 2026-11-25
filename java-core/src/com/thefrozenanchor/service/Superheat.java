package com.thefrozenanchor.service;


import com.thefrozenanchor.model.Refrigerant;

import java.util.InputMismatchException;
import java.util.Scanner;
/**
 * Interactive CLI service handler for calculating evaporator superheat.
 * Prompts the technician for suction line temperature measurements (in Fahrenheit or Celsius),
 * delegates the polymorphic superheat calculation to the active {@link Refrigerant} instance,
 * and displays the resulting temperature differential in both units.
 *
 * @author [Michael Newmarker], Retired USCG MKC
 */
public class Superheat {
    /**
     * Constructs and executes the interactive superheat calculation workflow for a given refrigerant.
     *
     * @param hvac1 The active {@link Refrigerant} instance (pure or blended) containing resolved saturation data.
     */
    public Superheat(Refrigerant hvac1){
        Scanner scnr = new Scanner(System.in);
        int calcChoice;
        int tempUnitChoice;
        double suctionLineTemp = 0.0;
        double superheatResult = 0.0;

        try {
            System.out.println("Would you like to calculate Superheat? ");
            System.out.print("Press 1 for Yes | Press 2 for No: ");
            calcChoice = scnr.nextInt();

            if (calcChoice == 1) {
                System.out.println();
                System.out.println("Reminder: Measure suction line temp as close to the compressor suction service valve as possible.");
                System.out.print("Press 1 for Fahrenheit or 2 for Celsius: ");
                tempUnitChoice = scnr.nextInt();

                if (tempUnitChoice == 1) {
                    System.out.println("Please enter the actual suction line temperature (F): ");
                    suctionLineTemp = scnr.nextDouble();
                } else if (tempUnitChoice == 2) {
                    System.out.println("Please enter actual suction line temperature (C): ");
                    double tempC = scnr.nextDouble();
                    suctionLineTemp = (tempC * 1.8) + 32;
                } else {
                    System.out.println("[!] Invalid unit choice. Returning to main menu...");
                    return;
                }
                // Polymorphic call: resolves against Dew Point for blends or Sat Temp for pure refrigerants
                superheatResult = hvac1.calculateSuperheat(suctionLineTemp);
                // Note: Temperature differentials (Delta-T) convert from F to C by dividing by 1.8 without subtracting 32
                System.out.printf("Calculated Superheat: %.1f F (%.1f C)%n", superheatResult, superheatResult / 1.8);
            } else if (calcChoice != 2) {
                System.out.println("[!] Invalid selection. Returning to main menu...");
            }
        }
        catch (InputMismatchException e) {
            System.out.println("[!] Invalid input: Please enter numbers only. Returning to main menu...");
            scnr.nextLine();
        }
    }
}
