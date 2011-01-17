package com.jpcamara.gosu.json;

import gw.lang.init.GosuPathEntry;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.module.Dependency;
import gw.lang.reflect.module.IDirectory;
import gw.lang.reflect.module.IFile;
import gw.lang.reflect.module.IModule;
import gw.lang.reflect.module.IResource;
import gw.lang.reflect.module.IResourceAccess;
import gw.lang.reflect.module.ITypeLoaderStack;
import gw.lang.reflect.module.ModuleClassLoader;
import gw.lang.reflect.module.IClassPath.ClassPathInfo;
import gw.util.Pair;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockModule implements IModule {

	@Override
	public IResourceAccess getResourceAccess() {
		// TODO Auto-generated method stub
		return new Access();
	}
	
	private static class Access implements IResourceAccess {

		private static final String PATH = 
			"/Users/johnpcamara/Projects/Playground/Gosu/GosuJsonType/src/com/jpcamara/gosu/json/";
		private static final List<File> FILES = Arrays.asList(new File(PATH).listFiles(
				new FilenameFilter() {
					@Override
					public boolean accept(File file, String name) {
						return name.endsWith(".json");
					}
				}));
		
		@Override
		public List<Pair<String, IFile>> findAllFilesByExtension(
				String extension) {
			List<Pair<String, IFile>> pairs = new ArrayList<Pair<String, IFile>>();
			for (final File f : FILES) {
				Pair<String, IFile> pair = new Pair<String, IFile>(
						f.getPath(),
						new IFile() {

							@Override
							public String getBaseName() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public String getExtension() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public InputStream openInputStream()
									throws IOException {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public OutputStream openOutputStream()
									throws IOException {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public OutputStream openOutputStreamForAppend()
									throws IOException {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public boolean delete() throws IOException {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean exists() {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public String getAbsolutePath() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public String getName() {
								// TODO Auto-generated method stub
								return f.getName();
							}

							@Override
							public IDirectory getParent() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public String getPath() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public boolean isChildOf(IDirectory dir) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean isDescendantOf(IDirectory dir) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean isJavaFile() {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public File toJavaFile() {
								// TODO Auto-generated method stub
								return f;
							}

							@Override
							public URI toURI() {
								// TODO Auto-generated method stub
								return null;
							}
							
						});
				pairs.add(pair);
			}
			// TODO Auto-generated method stub
			return pairs;
		}
		
		@Override
		public List<? extends IFile> findAllFiles(String resourceName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IFile findFirstFile(String resourceName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getResourceName(URL url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<? extends IDirectory> getRoots() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<? extends IDirectory> getSourceEntries() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<? extends IFile> iterateUniqueFilesWithinDirectory(
				String relativeDirName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String pathRelativeToRoot(IResource resource) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	@Override
	public void addDependency(Dependency dependency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPathEntry(GosuPathEntry pathEntry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addTypeLoader(ITypeLoader typeLoader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void callAll(Runnable preOrderOp, Runnable postOrderOp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearDependencies() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends ITypeLoader> List<? extends T> getAllTypeLoaders(
			Class<T> typeLoaderClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassNameForFile(File classFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassPathInfo getClassNamesFromClassPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Dependency> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getJavaClassPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ITypeLoader> List<? extends T> getLocalTypeLoaders(
			Class<T> typeLoaderClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypeLoaderStack getModuleTypeLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNativeModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgramNameForFile(File programFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IDirectory> getResourcePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDirectory getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IDirectory> getSourcePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTemplateNameForFile(File templateFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITypeLoader> getTypeLoaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> loadClass(String strJavaClassName)
			throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> loadClass(String strJavaClassName, boolean bResolve)
			throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshTypeloaders() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDependency(Dependency d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDependency(IModule module) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClassLoader(ModuleClassLoader classLoader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJavaClasspath(List<URL> paths) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJavaClasspathFromFiles(List<String> paths) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNativeModule(Object module) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRoot(IDirectory rootDir) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSourcePath(List<IDirectory> path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTypeloaderClassLoader(ClassLoader typeloaderClassLoader) {
		// TODO Auto-generated method stub

	}

	@Override
	public ContainerType getContainerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDirectory getOutputPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOutputPath(IDirectory path) {
		// TODO Auto-generated method stub

	}

}
