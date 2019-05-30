package org.pf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LegacyExtensionFinderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    /* Requires a JDK to run. Won't run on JRE. */
    public void shouldUnlockFileAfterReadingExtensionsFromPlugin() throws IOException {
        String pluginId = "plugin1";
        String pluginFolderName = "pluginsFolder";
        String pluginClassName = "DummyPlugin";
        String pluginSourceFileName = pluginClassName + ".java";
        String pluginClassFileName = pluginClassName + ".class";
        String extensionClassName = "DummyExtension";
        String pluginExtensionClassName = pluginClassName + "$" + extensionClassName;
        File pluginsFolder = testFolder.newFolder(pluginFolderName);

        File pluginClassSourceFile = createPluginSourceFile(pluginClassName, extensionClassName, pluginFolderName,
                pluginSourceFileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, pluginClassSourceFile.getPath());

        File pluginJar = createPluginJar(pluginId, pluginFolderName, pluginsFolder, pluginClassName,
                pluginClassFileName, pluginExtensionClassName);

        PluginManager pluginManager = new DefaultPluginManager(pluginsFolder.toPath());
        pluginManager.loadPlugins();
        LegacyExtensionFinder extensionFinder = new LegacyExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginStorages = extensionFinder.readPluginsStorages();
        pluginManager.unloadPlugin(pluginId);
        boolean fileDeleted = pluginJar.delete();

        assertThat(pluginStorages, is(notNullValue()));
        assertThat(pluginStorages.get(pluginId), is(notNullValue()));
        assertThat(pluginStorages.get(pluginId).size(), is(equalTo(1)));
        assertThat(pluginStorages.get(pluginId), contains(pluginExtensionClassName));
        assertThat(fileDeleted, is(equalTo(true)));
    }

    private File createPluginJar(String pluginId, String pluginFolderName, File pluginsFolder, String pluginClassName,
            String pluginClassFileName, String pluginExtensionClassName) throws IOException, FileNotFoundException {
        File pluginJar = testFolder.newFile(pluginFolderName + "/plugin1.jar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(pluginJar))) {
            zos.putNextEntry(new ZipEntry("META-INF/"));
            zos.putNextEntry(new ZipEntry("META-INF/extensions.idx"));
            writeToStream(pluginExtensionClassName, zos);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            List<String> manifestEntries = Arrays.asList("Plugin-Id: " + pluginId, "Plugin-Version: 0.0.1",
                    "Plugin-Class: " + pluginClassName);
            writeToStream(manifestEntries, zos);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry(pluginClassFileName));
            writeToStream(pluginsFolder.toPath().resolve(pluginClassFileName).toFile(), zos);
        }
        return pluginJar;
    }

    private File createPluginSourceFile(String pluginClassName, String extensionClassName, String pluginFolderName,
            String sourceFileName) throws IOException {
        File pluginClassSourceFile = testFolder.newFile(pluginFolderName + "/" + sourceFileName);
        // @formatter:off
        String dummyPluginSourceCode = new StringBuilder()
                .append("import org.pf4j.Extension;")
                .append("import org.pf4j.Plugin;")
                .append("import org.pf4j.PluginWrapper;")
                .append("import org.pf4j.ExtensionPoint;")
                .append("public class ").append(pluginClassName).append(" extends Plugin {")
                .append("public ").append(pluginClassName).append("(PluginWrapper wrapper){super( wrapper);}")
                .append("@Extension public static class ").append(extensionClassName).append(" implements ExtensionPoint{}")
                .append("}")
                .toString();
        // @formatter:on
        writeToFile(dummyPluginSourceCode, pluginClassSourceFile);
        return pluginClassSourceFile;
    }

    private void writeToStream(String data, ZipOutputStream zos) throws IOException {
        byte[] b = data.getBytes();
        zos.write(b, 0, b.length);
    }

    private void writeToStream(File sourceFile, ZipOutputStream zos) throws IOException {
        try (FileInputStream sourceInputStream = new FileInputStream(sourceFile)) {
            int c;
            while ((c = sourceInputStream.read()) != -1) {
                zos.write(c);
            }
        }
    }

    private void writeToStream(List<String> dataLines, ZipOutputStream zos) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(zos));
        for (String dataLine : dataLines) {
            bufferedWriter.write(dataLine);
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
    }

    private void writeToFile(String data, File file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            byte[] b = data.getBytes();
            os.write(b, 0, b.length);
        }
    }
}