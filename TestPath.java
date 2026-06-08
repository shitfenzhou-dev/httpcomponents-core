import org.apache.hc.core5.net.URIBuilder;
import java.net.URI;

public class TestPath {
    public static void main(String[] args) throws Exception {
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost("somehost")
                .setPath("api")
                .appendPath("");
        System.out.println("appendPath(\"\"): " + builder.build());

        builder = new URIBuilder()
                .setScheme("https")
                .setHost("somehost")
                .setPath("api")
                .appendPath("/");
        System.out.println("appendPath(\"/\"): " + builder.build());

        builder = new URIBuilder()
                .setScheme("https")
                .setHost("somehost")
                .setPath("api")
                .appendPath("//");
        System.out.println("appendPath(\"//\"): " + builder.build());
    }
}
