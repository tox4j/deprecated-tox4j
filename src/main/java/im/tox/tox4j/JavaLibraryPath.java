package im.tox.tox4j;

import java.io.File;
import java.lang.reflect.Field;


public class JavaLibraryPath {

  public static void addLibraryPath(String path) throws IllegalAccessException, NoSuchFieldException {
    Field field = ClassLoader.class.getDeclaredField("usr_paths");
    field.setAccessible(true);
    String[] paths = (String[]) field.get(null);
    for (String usr_path : paths) {
      if (path.equals(usr_path)) {
        return;
      }
    }
    String[] tmp = new String[paths.length + 1];
    System.arraycopy(paths, 0, tmp, 0, paths.length);
    tmp[paths.length] = path;
    field.set(null, tmp);
    System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + path);
  }

}
