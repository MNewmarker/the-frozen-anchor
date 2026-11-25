package com.thefrozenanchor;

import com.thefrozenanchor.model.*;
import com.thefrozenanchor.service.*;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Entry point and CLI controller for The Frozen Anchor HVACR utility.
 * Manages user interaction, determines refrigerant classification (pure vs. blended),
 * and routes pressure/temperature inputs to the appropriate calculation services.
 *
 * @author [Michael Newmarker], Retired USCG MKC
 */
public class Main {
    /**
     * Initializes the application loop, displays safety disclaimers, and handles
     * menu navigation for P/T chart lookups, superheat, and subcooling calculations.
     *
     * @param args Command-line arguments (not used).
     * @throws IOException If an error occurs while reading refrigerant TSV data tables.
     */
    public static void main(String[] args) throws IOException {
        Scanner scnr = new Scanner(System.in);

        String reeferType;
        double tempF = 0.0;
        double tempC;
        double dischargePressure;
        double suctionPressure;
        int appMenuChoice = 0;
        int menuChoice;
        int sideChoice;
        int tempUnitChoice;
        Refrigerant hvac1 = null;

        // Display industry-standard safety notice before allowing tool usage
        System.out.println("-".repeat(50));
        System.out.println("          THE FROZEN ANCHOR");
        System.out.println("          Disclaimer & Safety Notice\n" +
                "This application is intended strictly as a general reference and educational aid for qualified HVACR professionals.\n" +
                "It is not a comprehensive service guide and does not replace proper training, certification, or adherence to local\ncodes and regulations." +
                " Always follow the equipment manufacturer's official service instructions, charging specifications,\nand safety guidelines—particularly" +
                " when working with flammable (A2L/A3) or specialized refrigerants. By using this app,\nyou acknowledge that the developer assumes no responsibility " +
                "or liability for any service performed, equipment damage,\nor personal injury resulting from the use of this information.");
        System.out.println("-".repeat(50));
        System.out.print("Press 1 to begin: ");
        appMenuChoice = scnr.nextInt();

        while (appMenuChoice != 0) {
            try {
                System.out.println();
                System.out.println("0. EXIT");
                System.out.println("1. P/T Chart Lookup & Superheat / Subcooling Calculator");
                System.out.println("2. Chief's Playbook: Beginner Tips & Top Ten Basics ");
                System.out.println("3. Full System Troubleshooting Tree & Ask the Chief");
                System.out.println();
                System.out.print("Please enter your choice: ");


                appMenuChoice = scnr.nextInt();
                scnr.nextLine();
                if (appMenuChoice == 0) {
                    System.out.println("Stay Frosty! Goodbye!");
                    break;
                }
                else if (appMenuChoice == 1) {
                    char firstDigit = '0';
                    System.out.print("Please enter your refrigerant type: ");
                    reeferType = scnr.nextLine();
                    System.out.println();
                    // Extract the first numeric character to identify the ASHRAE refrigerant series
                    for (char c : reeferType.toCharArray()) {
                        if (Character.isDigit(c)) {
                            firstDigit = c;
                            break;
                        }

                    }
                    // Polymorphic instantiation: ASHRAE 400 (zeotropic) and 500 (azeotropic) series
                    // are blends requiring bubble/dew point handling, whereas others are single-component.
                    if (firstDigit == '4' || firstDigit == '5') {
                        hvac1 = new BlendedRefrigerant();
                    } else {
                        hvac1 = new PureRefrigerant();
                    }
                    hvac1.setRefrigerant(reeferType);


                    System.out.println("Press 1 to enter pressure: ");
                    System.out.println("Press 2 to enter temperature: ");
                    System.out.print("Please enter your choice: ");
                    menuChoice = scnr.nextInt();
                    System.out.println();
                    // Branch 1: Pressure-to-Temperature lookup + Superheat/Subcooling
                    if (menuChoice == 1) {
                        System.out.println("Is this the High Side (Discharge) pressure or Low Side (Suction) pressure?");
                        System.out.print("Press 1 for Discharge and 2 for Suction: ");
                        sideChoice = scnr.nextInt();

                        if (sideChoice == 1) {
                            System.out.print("Please provide your pressure reading (PSIG): ");
                            dischargePressure = scnr.nextDouble();
                            hvac1.setPsig(dischargePressure);
                            hvac1.setPT();
                            hvac1.printFormat();
                            SubCooling subcool = new SubCooling(hvac1);
                        } else if (sideChoice == 2) {
                            System.out.print("Please provide your pressure reading (PSIG): ");
                            suctionPressure = scnr.nextDouble();
                            hvac1.setPsig(suctionPressure);
                            hvac1.setPT();
                            hvac1.printFormat();
                            Superheat superheat = new Superheat(hvac1);
                        }
                    }
                    // Branch 2: Temperature-to-Pressure lookup (standardized internally to Fahrenheit)
                    else if (menuChoice == 2) {
                        System.out.println("Is your temperature in the units of Fahrenheit or Celsius?");
                        System.out.print("Press 1 for Fahrenheit and 2 for Celsius: ");
                        tempUnitChoice = scnr.nextInt();
                        scnr.nextLine();

                        if (tempUnitChoice == 1) {
                            System.out.print("Please provide your temperature in Fahrenheit: ");
                            tempF = scnr.nextDouble();
                            scnr.nextLine();
                        } else if (tempUnitChoice == 2) {
                            System.out.print("Please provide your temperature in Celsius: ");
                            tempC = scnr.nextDouble();
                            tempF = (tempC * 1.8) + 32;
                        } else {
                            System.out.println("Invalid temperature unit. Returning to main menu.");
                            continue;
                        }
                        hvac1.setTemp(tempF);
                        hvac1.setPT();
                        hvac1.printFormat();
                        System.out.println();
                    }
                }
                else if (appMenuChoice == 2) {
                    System.out.println("\n[Chief's Playbook] - Coming in v2!");
                }
                else if (appMenuChoice == 3) {
                    System.out.println("\n[Full System Troubleshooting Tree] - Coming in v2!]");
                }
                else {
                    System.out.println("\nInvalid choice. Please select 0, 1, 2, or 3.");
                }
            }
            catch (InputMismatchException e) {
                System.out.println("\n[!] Invalid input: Please enter numbers only where prompted. Returning to main menu...");
                scnr.nextLine();
            }
            catch (IOException | NullPointerException e) {
                System.out.println("\n[!] Refrigerant data file not found or unreadable. Check the refrigerant name and try again.");
            }
        }
    }
}