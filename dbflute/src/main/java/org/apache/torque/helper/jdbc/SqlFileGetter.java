package org.apache.torque.helper.jdbc;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class SqlFileGetter {
    private final FileFilter _sqlFileFileter = new FileFilter() {
        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".sql");
        }
    };

    private final FileFilter _directoryOnlyFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    protected FileFilter getSqlFileFilter() {
        return _sqlFileFileter;
    }

    protected FileFilter getDirectoryOnlyFilter() {
        return _directoryOnlyFilter;
    }

    public List<File> getSqlFileList(String sqlDirectory) {
        final List<File> fileList = new ArrayList<File>();
        {
            final File file = new File(sqlDirectory);
            registerFile(fileList, file);
        }
        return fileList;
    }

    protected void registerFile(List<File> fileList, File file) {
        final File[] sqlFiles = file.listFiles(_sqlFileFileter);
        final File[] directories = file.listFiles(_directoryOnlyFilter);
        for (final File sqlFile : sqlFiles) {
            fileList.add(sqlFile);
        }
        for (final File dir : directories) {
            registerFile(fileList, dir);
        }
    }
}
