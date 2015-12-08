package dae.samsungsdk.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * <p>A simple builder for the Samsung SDK, that packages may be built quickly for faster App testing.</p>
 * 
 * <p>For optimum power, include this in your automated build process in Eclipse (or your IDE of choice)</p>
 * 
 * @author David Edwards <knossos@gmail.com>
 *
 */
public class Main {
    @Option(name="-i",usage="input folder of your Samsung SDK Project",metaVar="INPUT")
    private File projectFolder;

    @Option(name="-w",usage="output location (directory, not file) of your widgetlist.xml",metaVar="OUTPUT")
    private File widgetlistFolder = new File(".");

    @Option(name="-W",usage="widget ID")
    private String widgetid = "WidgetId";

    @Option(name="-v",usage="package version (0.1)")
    private String version = "0.1";

    @Option(name="-n",usage="package name")
    private String name = "AppName";

    @Option(name="-r",usage="package region (Europe)")
    private String region = "Europe";

    @Option(name="-d",usage="package date (default today)")
    private String date = (new SimpleDateFormat("yyyyMMdd", Locale.US)).format(new Date());

    @Option(name="-I",usage="address of server (192.168.0.1)")
    private String ip = "192.168.0.1";

    @Option(name="-D",usage="description of App")
    private String description = "App description";

    @Option(name="-c",usage="on completion - give widgetlist path to external program")
    private File completionTarget = null;

    @Option(name="-V",usage="should we output to stdout")
    private boolean verboseMode;
	

    @Argument
    private List<String> arguments = new ArrayList<String>();

	public static void main(String[] args) {
		new Main(args);
	}
	
	public Main(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
        
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar sdkb.jar [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();

            return;
        }

        // Check that the SDK Project folder exists and is valid
        if(projectFolder == null || !projectFolder.exists() || !projectFolder.isDirectory()) {
        	if(verboseMode) System.out.println("The argument -i <PATH> must be provided, it must be a directory, and it must be the path of your Samsung SDK Project.");
        	return;
        }
        
        // Generate a random ID for the temporary folder and attempt to create it
        int randomId = (new Random().nextInt(99999999));
        File tempDirectory = new File("temp-builder-folder-"+randomId);
        if(!tempDirectory.mkdirs()) {
        	if(verboseMode) System.out.println("Failed to create a temporary directory for storing build files ("+tempDirectory.getAbsolutePath()+").");
        	return;
        }
        
        // Declare the Widget folder, create it if it does not exist
        File widgetFolder = new File(widgetlistFolder, "Widget");
        if(!widgetFolder.exists() && !widgetFolder.isDirectory()) {
	        if(!widgetFolder.mkdirs()) {
	        	if(verboseMode) System.out.println("Failed to create the Widget directory ("+widgetFolder.getAbsolutePath()+").");
	        	return;
	        }
        }
        
        // Declare the main config XML file
        File widgetlist = new File(widgetlistFolder, "widgetlist.xml");
        
        // Declare the Zip file
        File zip = new File(widgetFolder, name+"_"+version+"_"+region+"_"+date+".zip");
        
        try {
            if(verboseMode) System.out.println("Project folder: "+projectFolder.getAbsolutePath());
            if(verboseMode) System.out.println("Temporary folder: "+tempDirectory.getAbsolutePath());
            if(verboseMode) System.out.println("Output Widgetlist: "+widgetlistFolder.getAbsolutePath());
            if(verboseMode) System.out.println("Output Widget folder: "+widgetFolder.getAbsolutePath());
            if(verboseMode) System.out.println("Output Package file: "+zip.getAbsolutePath());
            if(verboseMode) System.out.println("Output Widgetlist: "+widgetlist.getAbsolutePath());

            if(verboseMode) System.out.println("Copying project folder to temporary directory");
			FileUtils.copyDirectory(projectFolder, tempDirectory, new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(file.getName().endsWith(".jar")) return false;
					return true;
				}
			});
            if(verboseMode) System.out.println("Deleting non-required files");
			FileUtils.deleteQuietly(new File(tempDirectory, ".project"));
			FileUtils.deleteQuietly(new File(tempDirectory, ".settings"));

            if(verboseMode) System.out.println("Building package");
			Zip z = new Zip();
			z.setSourcePath(tempDirectory);
			z.zipIt(zip.getAbsolutePath());
            if(verboseMode) System.out.println("Package is built");
		} 
        catch (IOException e) {
	        System.out.println("Could not build package!");
			e.printStackTrace();
			return;
		} finally {
            if(verboseMode) System.out.println("Deleting temporary directory");
	        FileUtils.deleteQuietly(tempDirectory);
		}
        
        // TODO: Template to be stored as a file to be imported. Will look much cleaner. 
        StringBuilder widgetlistTemplate = new StringBuilder();
        widgetlistTemplate.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n");
		widgetlistTemplate.append("<rsp stat=\"ok\">\r\n");
		widgetlistTemplate.append("<list>\r\n");
		widgetlistTemplate.append("<widget id=\""+widgetid+"\">\r\n");
		widgetlistTemplate.append("<title>"+name+"</title>\r\n");
		widgetlistTemplate.append("<compression size=\""+zip.length()+"\" type=\"zip\"/>\r\n");
		widgetlistTemplate.append("<description>"+description+"</description>\r\n");
		widgetlistTemplate.append("<download>http://"+ip+"/Widget/"+zip.getName()+"</download>\r\n");
		widgetlistTemplate.append("</widget>\r\n");
		widgetlistTemplate.append("</list>\r\n");
		widgetlistTemplate.append("</rsp>\r\n");

        try {
            if(verboseMode) System.out.println("Writing new widgetlist.xml");
			FileUtils.write(widgetlist, widgetlistTemplate.toString());
		}
		catch (IOException e) {
	        System.out.println("Could not write widgetlist.xml!");
			e.printStackTrace();
			return;
		}

        if(verboseMode) System.out.println("Build process completed");
        
        if(completionTarget != null) {
            if(verboseMode) System.out.println("Passing widgetlist folder to external program.");
            try {
				new ProcessBuilder(completionTarget.getAbsolutePath(), widgetlistFolder.getAbsolutePath()).start();
			}
			catch (IOException e) {
		        System.out.println("Could not launch external program!");
				e.printStackTrace();
				return;
			}
        }
	}

}
