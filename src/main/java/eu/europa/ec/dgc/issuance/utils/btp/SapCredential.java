package eu.europa.ec.dgc.issuance.utils.btp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;
import lombok.Data;

@Data
public class SapCredential {

    private String id;
    private String name;
    private Date modifiedAt;
    private String value;
    private String status;
    private String username;
    private String format;
    private String category;
    private String type;

    public static SapCredential fromJson(String rawJson) {
        return gson().fromJson(rawJson, SapCredential.class);
    }

    private static Gson gson() {
        return new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();
    }

}
