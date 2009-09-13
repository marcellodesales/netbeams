package org.netbeams.dsp.persistence.util;

import java.io.Writer;

import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;
import org.netbeams.dsp.ysi.SondeTestData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

/**
 * The DSPJSONUtil class is responsible for generating the JSON instances from the message contents. These instances of
 * JSON are used for the DSP Data Persistence, on the KVP (Key-Value Pair) table.
 * 
 * @author Marcello de Sales (msales@sfsu.edu)
 */
public enum DSPJSONUtil {

    /**
     * Creates an instance of DSP JSON Utility to output JSON instances in an hierarchical way. Consider the following
     * example. <BR>
     * 
     * <pre>
     *    {&quot;product&quot;: {
     *         &quot;name&quot;: &quot;Marcello&quot;,
     *         &quot;age&quot;: 29
     *    }}
     * </pre>
     */
    HIERARCHICAL(true),
    /**
     * Creates an instance of DSP JSON Utility to output JSON instances in streams of characters. Consider the following
     * example.
     * 
     * <pre>
     *    {&quot;product&quot;:{&quot;name&quot;:&quot;Marcello&quot;,&quot;age&quot;:29}}
     * </pre>
     */
    STREAM(false);

    /**
     * Flag for the output type
     */
    private boolean hierarchicalOutput;

    /**
     * Constructs a new DSP JSON Util with a give type of output.
     * 
     * @param hierarchicalOutput
     */
    private DSPJSONUtil(boolean hierarchicalOutput) {
        this.hierarchicalOutput = hierarchicalOutput;
    }

    private HierarchicalStreamDriver getStreamJSONDriver() {
        return new JettisonMappedXmlDriver();
    }

    private HierarchicalStreamDriver getHierarchicalJSONDriver() {
        return new JsonHierarchicalStreamDriver();
    }

    private HierarchicalStreamDriver getStreamJSONDroppingRoot() {
        return new JettisonMappedXmlDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        };
    }

    private HierarchicalStreamDriver getHierarchicalJSONDroppingRoot() {
        return new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        };
    }

    private XStream getHierachicalJSON() {
        XStream xstream = new XStream(this.getHierarchicalJSONDroppingRoot());
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream;
    }

    private XStream getStreamJSON() {
        XStream xstream = new XStream(this.getHierarchicalJSONDroppingRoot());
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream;
    }

    private XStream buildXStreamForDSP(Class<? extends MessageContent> pojoClass, boolean hierOutput) {
        XStream jsonUtil = hierOutput ? this.getHierachicalJSON() : this.getStreamJSON();
        String[] simpleNames = pojoClass.getSimpleName().split("\\.");
        jsonUtil.alias(simpleNames[simpleNames.length - 1].toLowerCase(), pojoClass);
        return jsonUtil;
    }

    /**
     * Marshalls the content of the message content into a stream. It uses the notation starting with a root value
     * with the name of the JAVA class in lower-case.
     * 
     * @param pojoClass is an instance of a class of MessageContent.
     * @param inst is an instance of the message content class
     * @return the JSON representation of the given MessageContent instance.
     */
    public String marshallMessageContent(MessageContent inst) {
        XStream jsonUtil = this.buildXStreamForDSP(inst.getClass(), this.hierarchicalOutput);
        return jsonUtil.toXML(inst);
    }

    /**
     * Unmarshall the given JSON string into an instance of message content. It must contain the first root value
     * of the JAVA class notation in lower-case as the starting element.
     * 
     * @param pojoClass is an instance of the class to the string.
     * @param jsonStr is the JSON string that represents the POJO.
     * @return the message content instance as a POJO.
     */
    public MessageContent unmarshallMessageContent(Class<? extends MessageContent> pojoClass, String jsonStr) {
        XStream jsonUtil = this.buildXStreamForDSP(pojoClass, false);
        return (MessageContent) jsonUtil.fromXML(jsonStr);
    }

    private static class Product extends MessageContent {

        private String name;
        private int age;

        public Product() {
            this.name = "Marcello";
            this.age = 29;
        }

        @Override
        public String toString() {
            return this.name + " aged at " + this.age;
        }

        @Override
        public String toXml() {
            return null;
        }
    }

    public static void main(String[] args) {
        Product p = new Product();
        String jsonStr = DSPJSONUtil.STREAM.marshallMessageContent(p);
        System.out.println(jsonStr);
        System.out.println("-------------------------------------------");
//        System.out.println(DSPJSONUtil.STREAM.unmarshallMessageContent(Product.class, jsonStr));
        
        SondeDataContainer container = SondeTestData.generateRandomSondeDataContainer(2);
        String jsonStrCont = DSPJSONUtil.STREAM.marshallMessageContent(container);
        System.out.println(jsonStrCont);
        System.out.println("-------------------------------------------");
//      
    }
}
