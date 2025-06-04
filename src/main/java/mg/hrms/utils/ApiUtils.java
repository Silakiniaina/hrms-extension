package mg.hrms.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;

import mg.hrms.models.User;

public class ApiUtils {

    /* -------------------------------------------------------------------------- */
    /*                     Set the user cookien on the headers                    */
    /* -------------------------------------------------------------------------- */
    public static void setUserCookie(User user, HttpHeaders headers) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getSid() == null || user.getSid().isEmpty()) {
            throw new IllegalArgumentException("User sid cannot be null or empty");
        }

        headers.add("Cookie", "sid=" + user.getSid());
        headers.add("Cookie", "user_id=" + (user.getUserId() != null ? user.getUserId() : ""));
        headers.add("Cookie", "full_name=" + (user.getFullName() != null ? user.getFullName() : ""));
        headers.add("Cookie", "user_lang=" + (user.getUserLang() != null ? user.getUserLang() : ""));
        headers.add("Cookie", "system_user=" + (user.getSystemUser() != null ? user.getSystemUser() : ""));
    }

    /* -------------------------------------------------------------------------- */
    /*                       Build url for ErpNext API call                       */
    /* -------------------------------------------------------------------------- */
    public static String buildUrl(String baseUrl, String[] fields, List<String[]> filters) {
        try {
            // Convert the fields array to a JSON array string
            StringBuilder fieldsJson = new StringBuilder("[");
            for (int i = 0; i < fields.length; i++) {
                fieldsJson.append("\"").append(fields[i]).append("\"");
                if (i < fields.length - 1) {
                    fieldsJson.append(",");
                }
            }
            fieldsJson.append("]");

            // Start building the URL manually
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?fields=").append(URLEncoder.encode(fieldsJson.toString(), StandardCharsets.UTF_8));

            // Add filters if provided - ERPNext expects filters as array of arrays: [["field","operator","value"],...]
            if (filters != null && !filters.isEmpty()) {
                StringBuilder filtersJson = new StringBuilder("[");
                for (int i = 0; i < filters.size(); i++) {
                    if (i > 0) {
                        filtersJson.append(",");
                    }
                    String[] filter = filters.get(i);
                    filtersJson.append("[\"").append(filter[0]).append("\",\"").append(filter[1]).append("\",\"").append(filter[2]).append("\"]");
                }
                filtersJson.append("]");

                // URL encode the filters array
                String encodedFilters = URLEncoder.encode(filtersJson.toString(), StandardCharsets.UTF_8);
                urlBuilder.append("&filters=").append(encodedFilters);
            }

            return urlBuilder.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build URL: " + e.getMessage(), e);
        }
    }

    public static List<String[]> buildDobFilters(int minAge, int maxAge) {
        List<String[]> filters = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (minAge > 0 && maxAge > 0) {
            // For age range: person should be born after (today - maxAge - 1) and before (today - minAge + 1)
            LocalDate minDob = today.minusYears(maxAge + 1).plusDays(1);
            LocalDate maxDob = today.minusYears(minAge);

            filters.add(new String[]{"date_of_birth", ">=", minDob.format(formatter)});
            filters.add(new String[]{"date_of_birth", "<=", maxDob.format(formatter)});
        } else if (minAge > 0) {
            // Person should be at least minAge years old: born before (today - minAge)
            LocalDate maxDob = today.minusYears(minAge);
            filters.add(new String[]{"date_of_birth", "<=", maxDob.format(formatter)});
        } else if (maxAge > 0) {
            // Person should be at most maxAge years old: born after (today - maxAge - 1)
            LocalDate minDob = today.minusYears(maxAge + 1).plusDays(1);
            filters.add(new String[]{"date_of_birth", ">=", minDob.format(formatter)});
        }

        return filters;
    }
}
