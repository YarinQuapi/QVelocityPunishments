package me.yarinlevi.qpunishments.utilities;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author YarinQuapi
 */
public class Configuration {
    private Yaml yaml = new Yaml();
    private File configFile = null;
    private Map<String, Object> result = new HashMap<>();

    public static Configuration load(String filePath) {
        return new Configuration(filePath);
    }

    /**
     * Initialize the configuration
     **/
    public Configuration(String filePath) {
        System.out.println("Attempting to load configuration: " + filePath);
        this.loadConfig(filePath);
    }

    public Integer getId() {
        return this.getInt("config-id");
    }

    public Collection<String> getKeys()
    {
        return new LinkedHashSet<>( result.keySet() );
    }

    @SuppressWarnings("unchecked")
    public void loadConfig(String filePath) {
        System.out.println("Loading configuration ...");
        configFile = new File(filePath);
        if (!configFile.exists()) {
            // Create file
            try {
                InputStream jarURL = this.getClass().getResourceAsStream(
                        "/" + filePath);
                if (jarURL != null) {
                    System.out.println("Copying '" + configFile
                            + "' from the resources!");
                    copyFile(jarURL, configFile);
                } else {
                    System.out.println("Configuration file not found inside the application!");
                }
                jarURL.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (configFile.exists()) {
            // Load the configuration file
            try {
                InputStream ios = null;
                ios = new FileInputStream(configFile);
                result = yaml.load(ios);
                ios.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    @SuppressWarnings("unchecked")
    public Object get(String path) {
        String[] pathArr = path.split("\\.");
        if (pathArr.length == 0) {
            pathArr = new String[1];
            pathArr[0] = path;
        }
        Object lastObj = this.result;
        for (int i = 0; i < pathArr.length; i++) {
            lastObj = ((Map<String, Object>) lastObj).get(pathArr[i]);
        }
        return lastObj;
    }

    /**
     * Get boolean value from configuration
     *
     * @param path
     *            Configuration path
     * @return Boolean value
     */
    public boolean getBoolean(String path) {
        Object lastObj = get(path);
        if (lastObj instanceof Boolean)
            return (Boolean) lastObj;
        return false;
    }

    /**
     * Get integer value from configuration
     *
     * @param path
     *            Configuration path
     * @return Integer result
     */
    public int getInt(String path) {
        Object lastObj = get(path);
        if (lastObj instanceof Integer)
            return (Integer) lastObj;
        return 0;
    }

    /**
     * Get string value from configuration
     *
     * @param path
     *            Configuration path
     * @return String result
     */
    public String getString(String path) {
        Object lastObj = get(path);
        if (lastObj instanceof String)
            return (String) lastObj;
        return "";
    }

    /**
     * Copies a file to a new location.
     *
     * @param in
     *            InputStream
     * @param out
     *            File
     *
     * @throws Exception
     */
    private static void copyFile(InputStream in, File out) throws Exception {
        InputStream fis = in;
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
