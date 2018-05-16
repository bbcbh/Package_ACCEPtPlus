package simulation;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 *
 * @author Ben Hui
 */
public class Simulation_Population_ACCEPtPlus {

    public static final String DEFAULT_DIR_PATH = "";
    public final String FILE_NAME_PROP = "simSpecificSim.prop";

    private final File baseDir;
    private final Path propFile;
    private final Properties prop;

    public Simulation_Population_ACCEPtPlus(File baseDir) {
        this.baseDir = baseDir;
        this.propFile = new File(baseDir, FILE_NAME_PROP).toPath();
        this.prop = new Properties();
    }

    public void loadProperties() throws IOException {
        try (InputStream inStr = java.nio.file.Files.newInputStream(propFile)) {
            prop.loadFromXML(inStr);
        }
    }

    public Properties getProperties() {
        return prop;
    }
    
    public void generateOneResultsSet(){
        
        
        
        
    }
    
    
    

    public static void main(String[] arg) throws IOException {
        File baseDir = new File(DEFAULT_DIR_PATH);
        File[] grpDirs;

        if (arg.length > 0) {
            baseDir = new File(arg[0]);
        }

        grpDirs = baseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File grpDir : grpDirs) {
            Simulation_Population_ACCEPtPlus sim = new Simulation_Population_ACCEPtPlus(grpDir);
            sim.loadProperties();

        }

    }

}
