package im.tox.tox4j;

import java.io.File;
import java.lang.reflect.Field;

public final class JavaLibraryPath {

  /**
   * Appends a path to the java.library.path property and to {@link ClassLoader}'s private static
   * {@link ClassLoader#usr_paths} String array field.
   *
   * @param path Path to add to the property and ClassLoader search path.
   * @throws IllegalAccessException If the security context did not allow writing to private fields.
   */
  public static void addLibraryPath(String path) throws IllegalAccessException {
    Field field = null;
    try {
      field = ClassLoader.class.getDeclaredField("usr_paths");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Implementation of ClassLoader changed: usr_paths field no longer exists", e);
    }
    field.setAccessible(true);
    String[] paths = (String[]) field.get(null);
    for (String usrPath : paths) {
      if (path.equals(usrPath)) {
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
