package global.smartup.node.constant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 将properties的key转化为常量
 */
public class PropConstGenerator {

    private static final String propPath = "/i18n/language.properties";

    private static final String generatorToPath = "\\src\\main\\java\\global\\smartup\\node\\constant\\";

    private static final String packageName = "global.smartup.node.constant";

    private static final String className = "LangHandle";


    public static void main(String[] args) throws IOException, URISyntaxException {

        String projectRoot = new File("").getAbsolutePath();
        File file = new File(projectRoot + generatorToPath + className + ".java");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        append(writer, "package " + packageName + ";");
        append(writer, "");
        append(writer, "/* generate by PropConstGenerator.java */");
        append(writer, "public class " + className + " {");

        InputStream is = PropConstGenerator.class.getResourceAsStream(propPath);
        Properties prop = new Properties();
        prop.load(is);
        Enumeration eunm = prop.propertyNames();

        while (eunm.hasMoreElements()) {
            Object o = eunm.nextElement();
            String fieldName = trans(o.toString());
            append(writer, "");
            append(writer, "\tpublic static final String " + fieldName + " = \"" + o.toString() + "\";");
        }

        append(writer, "");
        append(writer, "}");
        writer.flush();
        writer.close();

        System.out.println("===========================");
        System.out.println("PropConstGenerator run over");
        System.out.println("===========================");
    }

    public static void append(FileWriter writer, String line) throws IOException {
        writer.append(line).append(System.lineSeparator());
    }

    public static String trans(String propKey) {
        StringBuffer buf = new StringBuffer();
        if (propKey != null) {
            char[] arr = propKey.toCharArray();
            boolean f = true;
            for (int i = 0; i < arr.length; i++) {
                if (f) {
                    buf.append((char)(arr[i] - 32));
                    f = false;
                    continue;
                }
                if (arr[i] == '_') {
                    f = true;
                    continue;
                }
                buf.append(arr[i]);
            }
        }
        return buf.toString();
    }

}
