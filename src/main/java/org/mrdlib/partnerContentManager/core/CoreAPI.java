import org.mrdlib.api.manager.Constants;


public class CoreAPI {
    private String apiKey;

    public CoreAPI() {
        apiKey = new Constants().getCoreAPIKey();
    }
}