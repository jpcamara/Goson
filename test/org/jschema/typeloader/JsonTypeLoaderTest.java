package org.jschema.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.IResource;
import gw.fs.ResourcePath;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.module.*;
import gw.util.Pair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonTypeLoaderTest {

  @Test
  public void instantiate() throws Exception {
    new JsonTypeLoader(new MockModule());
  }

  @Test
  public void initTypes() throws Exception {
    JsonTypeLoader loader = new JsonTypeLoader(new MockModule());
    assertNotNull(loader.getType("jschema.example.Example"));
  }

  static class MockModule implements IModule {

    @Override
    public IResourceAccess getResourceAccess() {
      return new IResourceAccess() {
        @Override
        public List<Pair<String, IFile>> findAllFilesByExtension(String s) {
          File srcDir = new File("src");
          List<File> filesWithExt = find(srcDir, s);
          List<Pair<String, IFile>> pairs = new ArrayList<Pair<String, IFile>>();
          for (final File f : filesWithExt) {
            pairs.add(new Pair<String, IFile>(f.getName(), new IFile() {
              @Override
              public String getName() {
                int lastSep = f.getName().lastIndexOf(File.separator);
                return f.getName().substring(lastSep + 1);  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public File toJavaFile() {
                return f;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public InputStream openInputStream() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public OutputStream openOutputStream() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public OutputStream openOutputStreamForAppend() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public String getExtension() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public String getBaseName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public IDirectory getParent() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public boolean exists() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public boolean delete() throws IOException {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public URI toURI() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public ResourcePath getPath() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public boolean isChildOf(IDirectory iDirectory) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public boolean isDescendantOf(IDirectory iDirectory) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
              }

              @Override
              public boolean isJavaFile() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
              }
            }));
          }
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        private List<File> find(final File file, final String ext) {
          List<File> files = new ArrayList<File>();
          for (String dirOrFile : file.list()) {
            File current = new File(dirOrFile);
            if (current.isDirectory()) {
              files.addAll(find(current, ext));
            } else if (current.getName().endsWith(ext)) {
              files.add(current);
            }
          }
          return files;
        }

        @Override
        public List<? extends IDirectory> getRoots() {
          return null;
        }

        @Override
        public List<? extends IDirectory> getSourceEntries() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public IFile findFirstFile(String s) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<? extends IFile> findAllFiles(String s) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public IResourceAccess clearCaches() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Iterable<? extends IFile> iterateUniqueFilesWithinDirectory(String s) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String pathRelativeToRoot(IResource iResource) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getResourceName(URL url) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
      };
    }

    @Override
    public void update() {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addRoot(IDirectory iDirectory) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Dependency> getDependencies() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addDependency(Dependency dependency) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeDependency(Dependency dependency) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeDependency(IModule iModule) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clearDependencies() {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ITypeLoaderStack getModuleTypeLoader() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<IDirectory> getSourcePath() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSourcePath(List<IDirectory> iDirectories) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ModuleClassLoader getClassLoader() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setClassLoader(ModuleClassLoader moduleClassLoader) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addGosuApiPath(List<String> strings) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJavaClasspath(List<URL> urls) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJavaClasspathFromFiles(List<String> strings) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getJavaClassPath() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getNativeModule() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNativeModule(INativeModule iNativeModule) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isGosuModule() {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getClassNameForFile(File file) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTemplateNameForFile(File file) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getProgramNameForFile(File file) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IClassPath.ClassPathInfo getClassNamesFromClassPath() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URL getResource(String s) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refreshTypeloaders() {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends ITypeLoader> List<? extends T> getTypeLoaders(Class<T> tClass) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<IModule> getModuleTraversalList() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<IModule> getAllModuleDependencies() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean includesGosuCoreAPI() {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ContainerType getContainerType() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IDirectory getOutputPath() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setOutputPath(IDirectory iDirectory) {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }
}
