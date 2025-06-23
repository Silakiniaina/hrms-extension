package mg.hrms.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.slf4j.Logger;

import mg.hrms.exception.DateFormatException;

public class OperationUtils {
    
    /* -------------------------------------------------------------------------- */
    /*                             Log operation steps                            */
    /* -------------------------------------------------------------------------- */
    public static void logStep(String message, Logger logger) {
        logger.info(message);
    }

    /* -------------------------------------------------------------------------- */
    /*                          Format date to mm/DD/yyyy                         */
    /* -------------------------------------------------------------------------- */
    public static String formatDate(String date) throws DateFormatException {
        if (date == null)
            return null;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE);
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.format(formatter); 
        } catch (DateTimeParseException e) {
            throw new DateFormatException("Format date not valid, format should be : dd/MM/yyyy");
        }
    }
}
