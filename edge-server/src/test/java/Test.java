import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.edge.common.data.DataTag;
import com.my.edge.server.demo.DemoDataTag;
import com.my.edge.server.demo.DemoNodeMetadata;

import java.util.HashSet;
import java.util.Set;

/**
 * Creator: Beefman
 * Date: 2018/8/30
 */
public class Test {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(DemoDataTag.DEMO_DATA_TAG_1);
        System.out.println(value);
        DataTag anotherTag = mapper.readValue(value, DataTag.class);
        System.out.println(anotherTag.equals(DemoDataTag.DEMO_DATA_TAG_1));

        System.out.println("");
        DemoNodeMetadata demoNodeMetadata = new DemoNodeMetadata();
        demoNodeMetadata.setType("demo");
        demoNodeMetadata.setLocation("all");
        Set<DataTag> dataTags = new HashSet<>();
        dataTags.add(DemoDataTag.DEMO_DATA_TAG_1);
        demoNodeMetadata.setAccessibleDataTags(dataTags);
        value = mapper.writeValueAsString(demoNodeMetadata);
        System.out.println(value);
    }
}
