package mg.hrms.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

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
    @SuppressWarnings("deprecation")
    public static String buildUrl(String baseUrl, String[] fields) {
        // Convert the fields array to a JSON array string
        StringBuilder fieldsJson = new StringBuilder("[");
        for (int i = 0; i < fields.length; i++) {
            fieldsJson.append("\"").append(fields[i]).append("\"");
            if (i < fields.length - 1) {
                fieldsJson.append(",");
            }
        }
        fieldsJson.append("]");

        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParam("fields", fieldsJson.toString())
                .build()
                .toUriString();
    }
}
