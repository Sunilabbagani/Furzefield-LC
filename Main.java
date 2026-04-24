
package flc;

import flc.data.DataInitializer;

import flc.service.SystemManager;
import flc.ui.SmartConsoleUI;

/**
 * Entry point for the Furzefield Leisure Centre Booking System.
 *
 * Initialises sample data then launches the menu-driven CLI.
 */
public class 2Main {
    public static void main(String[] args) {
        SystemManager sm = new SystemManager();
        DataInitializer.populate(sm);

        SmartConsoleUI ui = new SmartConsoleUI(sm);
        ui.start();
    }
}
