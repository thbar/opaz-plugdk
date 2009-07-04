// tools shared between the two proxies
// TODO: move this logic to jVSTwRapper directly ?
public class ProxyTools {
  // Although I wanted to use VSTPluginAdapter.RUNNING_MAC_X instead of this, it raises AWT threads errors.
  // Sticking with this one for the moment.
  public static boolean useMacOSX() {
    String lcOSName = System.getProperty("os.name").toLowerCase();
    return lcOSName.startsWith("mac os x");
  }
  
  public static String getResourcesFolder(String logBasePath) {
    String resourcesFolder = logBasePath;
    if (useMacOSX()) // mac os x tweak :o
      resourcesFolder += "/../Resources";
    return resourcesFolder;
  }
  
  public static String getIniFileName(String resourcesFolder, String logFileName) {
    String iniFileName = logFileName.replaceAll("_java_stdout.txt","");
    if (useMacOSX())
      iniFileName += ".jnilib";
    return resourcesFolder + "/" + iniFileName + ".ini";
  }
}