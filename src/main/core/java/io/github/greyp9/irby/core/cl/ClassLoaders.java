package io.github.greyp9.irby.core.cl;

import io.github.greyp9.arwo.core.file.find.FindInFolderQuery;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.irby.core.cl.config.ClassLoaderConfig;
import io.github.greyp9.irby.core.cl.config.ResourceConfig;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public final class ClassLoaders {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Map<String, URLClassLoader> classLoaders;

    public ClassLoader getClassLoader(final String key) {
        return classLoaders.get(key);
    }

    public ClassLoaders(final Collection<ClassLoaderConfig> classLoaderConfigs, final ClassLoader classLoader) {
        this.classLoaders = new TreeMap<>();
        for (final ClassLoaderConfig classLoaderConfig : classLoaderConfigs) {
            classLoaders.put(classLoaderConfig.getName(), createClassLoader(classLoaderConfig, classLoader));
        }
    }

    private URLClassLoader createClassLoader(final ClassLoaderConfig classLoaderConfig, final ClassLoader parent) {
        final List<URL> urls = new ArrayList<>();
        for (final ResourceConfig resource : classLoaderConfig.getResources()) {
            final File file = new File(SystemU.userDir(), resource.getPath());
            final Collection<File> files = new FindInFolderQuery(
                    file.getParentFile(), file.getName(), false).getFound();
            for (File fileIt : files) {
                try {
                    urls.add(fileIt.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.severe(file.getAbsolutePath());
                }
            }
        }
        final URL[] urlsArray = urls.toArray(new URL[0]);
        return new URLClassLoader(urlsArray, parent);
    }
}
