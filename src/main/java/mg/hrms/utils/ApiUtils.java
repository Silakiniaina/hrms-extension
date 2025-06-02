package mg.hrms.utils;

import org.springframework.http.HttpHeaders;

import mg.hrms.models.User;

public class ApiUtils {

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
}
