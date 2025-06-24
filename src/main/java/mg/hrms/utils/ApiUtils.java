package mg.hrms.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;


import mg.hrms.models.User;

public class ApiUtils {

    /* -------------------------------------------------------------------------- */
    /* Set the user cookie on the headers                                */
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
    /* Build URL for individual resource requests                                 */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("deprecation")
    public static String buildResourceUrl(String baseUrl, String doctype, String resourceId, String[] fields) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("api", "resource", doctype, resourceId);

            if (fields != null && fields.length > 0) {
                // Construct the JSON array string for fields
                StringBuilder fieldsJson = new StringBuilder("[");
                for (int i = 0; i < fields.length; i++) {
                    fieldsJson.append("\"").append(fields[i]).append("\"");
                    if (i < fields.length - 1) {
                        fieldsJson.append(",");
                    }
                }
                fieldsJson.append("]");

                // Pass the raw JSON string. Encoding will happen later.
                builder.queryParam("fields", fieldsJson.toString());
            }

            // Corrected: Build the URI first, then encode it.
            return builder.build().encode().toUriString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build resource URL: " + e.getMessage(), e);
        }
    }


    /* -------------------------------------------------------------------------- */
    /*                   Build url for list, collection request                   */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("deprecation")
    public static String buildUrl(String baseUrl, String doctype, String[] fields, List<String[]> filters) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("api", "resource", doctype); // Assuming doctype is part of the path for collection requests

            // Add fields if provided
            if (fields != null && fields.length > 0) {
                StringBuilder fieldsJson = new StringBuilder("[");
                for (int i = 0; i < fields.length; i++) {
                    fieldsJson.append("\"").append(fields[i]).append("\"");
                    if (i < fields.length - 1) {
                        fieldsJson.append(",");
                    }
                }
                fieldsJson.append("]");
                // Pass the raw JSON string; Encoding will happen later.
                builder.queryParam("fields", fieldsJson.toString());
            }

            // Add filters if provided - ERPNext expects filters as array of arrays: [["field","operator","value"],...]
            if (filters != null && !filters.isEmpty()) {
                StringBuilder filtersJson = new StringBuilder("[");
                for (int i = 0; i < filters.size(); i++) {
                    if (i > 0) {
                        filtersJson.append(",");
                    }
                    String[] filter = filters.get(i);
                    // Ensure filter[0], filter[1], filter[2] are not null to avoid NullPointerException
                    // Add quotes around each element as per ERPNext filter array format
                    filtersJson.append("[\"")
                        .append(filter[0] != null ? filter[0] : "")
                        .append("\",\"")
                        .append(filter[1] != null ? filter[1] : "")
                        .append("\",\"")
                        .append(filter[2] != null ? filter[2] : "")
                        .append("\"]");
                }
                filtersJson.append("]");
                // Pass the raw JSON string; Encoding will happen later.
                builder.queryParam("filters", filtersJson.toString());
            }

            return builder.build().encode().toUriString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build URL: " + e.getMessage(), e);
        }
    }


    /* -------------------------------------------------------------------------- */
    /*                         Build day of birth filters                         */
    /* -------------------------------------------------------------------------- */
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
