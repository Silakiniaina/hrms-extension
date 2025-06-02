package mg.hrms.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${erpnext.server.url}")
    private String serverHost;

    /* -------------------------------------------------------------------------- */
    /*                                   Getter                                   */
    /* -------------------------------------------------------------------------- */
    public String getServerHost(){
        return this.serverHost;
    }

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public RestApiService(RestTemplate r){
        this.restTemplate = r;
    }
}
