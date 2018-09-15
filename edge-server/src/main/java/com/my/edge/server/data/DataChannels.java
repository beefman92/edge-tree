package com.my.edge.server.data;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Creator: Beefman
 * Date: 2018/8/9
 */
public class DataChannels {
    private Map<String, DataImport> imports = new HashMap<>();
    private Map<String, DataExport> exports = new HashMap<>();
    private Map<String, String> importToExport = new HashMap<>();
    private Map<String, String> exportToImport = new HashMap<>();
    private Map<Object, Integer> referenceCounts = new HashMap<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private int countPlusOne(Object object) {
        if (object == null) {
            throw new RuntimeException("FUCK! ");
        }
        Integer count = referenceCounts.get(object);
        if (count == null || count == 0) {
            referenceCounts.put(object, 1);
            return 1;
        } else {
            count++;
            referenceCounts.put(object, count);
            return count;
        }
    }

    private int countMinusOne(Object object) {
        if (object == null) {
            throw new RuntimeException("FUCK, TOO! ");
        }
        Integer count = referenceCounts.get(object);
        if (count == null || count == 0) {
            throw new RuntimeException("Reference leaks. Reference count for " + object + " less than dereference times. ");
        } else {
            count--;
            referenceCounts.put(object, count);
            return count;
        }
    }

    public void addChannel(DataImport dataImport, DataExport dataExport) {
        lock.writeLock().lock();
        try {
            countPlusOne(dataImport);
            countPlusOne(dataExport);
            imports.put(dataImport.getId(), dataImport);
            exports.put(dataExport.getId(), dataExport);
            importToExport.put(dataImport.getId(), dataExport.getId());
            exportToImport.put(dataExport.getId(), dataImport.getId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addChannel(DataImport dataImport, String dataExportId) {
        lock.writeLock().lock();
        try {
            DataExport dataExport = exports.get(dataExportId);
            if (dataExport == null) {
                throw new RuntimeException("DataExport " + dataExportId + " does not exists. ");
            }
            countPlusOne(dataImport);
            countPlusOne(dataExport);
            imports.put(dataImport.getId(), dataImport);
            importToExport.put(dataImport.getId(), dataExportId);
            exportToImport.put(dataExportId, dataImport.getId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addDataExport(DataExport dataExport) {
        lock.writeLock().lock();
        try {
            countPlusOne(dataExport);
            exports.put(dataExport.getId(), dataExport);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addDataImport(DataImport dataImport) {
        lock.writeLock().lock();
        try {
            countPlusOne(dataImport);
            imports.put(dataImport.getId(), dataImport);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public DataExport getDataExport(String dataExportId) {
        lock.readLock().lock();
        try {
            return exports.get(dataExportId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean hasDataExport(String dataExportId) {
        lock.readLock().lock();
        try {
            return exports.containsKey(dataExportId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public DataExport getDataExportByDataImportId(String dataImportId) {
        lock.readLock().lock();
        try {
            String exportId = importToExport.get(dataImportId);
            if (exportId == null) {
                return null;
            } else {
                DataExport dataExport = exports.get(exportId);
                return dataExport;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private DataImport removeDataImportInternal(String dataImportId) {
        DataImport dataImport = imports.get(dataImportId);
        if (dataImport == null) {
            throw new RuntimeException("Id " + dataImportId + " does not match any DataImport. ");
        }
        if (countMinusOne(dataImport) == 0) {
            imports.remove(dataImportId);
        }
        return dataImport;
    }

    private DataExport removeDataExportInternal(String dataExportId) {
        DataExport dataExport = exports.get(dataExportId);
        if (dataExport == null) {
            throw new RuntimeException("Id " + dataExportId + " does not match any DataExport. ");
        }
        if (countMinusOne(dataExport) == 0) {
            exports.remove(dataExportId);
        }
        return dataExport;
    }

    public Set<DataExport> removeChannelsByImportsId(Collection<String> importsId) {
        lock.writeLock().lock();
        try {
            Set<DataExport> removedDataExports = new HashSet<>();
            for (String importId : importsId) {
                removeDataImportInternal(importId);

                String exportId = importToExport.remove(importId);
                if (exportId == null) {
                    throw new RuntimeException("Cannot find DataExport by DataImport id " + importId);
                }
                exportToImport.remove(exportId);

                DataExport dataExport = removeDataExportInternal(exportId);
                removedDataExports.add(dataExport);
            }
            return removedDataExports;
        } finally {
            lock.writeLock().unlock();
        }
    }



    public DataExport removeChannelByImportId(String importId) {
        lock.writeLock().lock();
        try {
            removeDataImportInternal(importId);
            String exportId = importToExport.remove(importId);
            exportToImport.remove(exportId);
            DataExport dataExport = removeDataExportInternal(exportId);
            return dataExport;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 只有在请求数据的节点未能请求到数据时才调用
    public void removeDataImport(String dataImportId) {
        lock.writeLock().lock();
        try {
            removeDataImportInternal(dataImportId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<DataImport> removeChannelsByExportsId(Collection<String> exportsId) {
        lock.writeLock().lock();
        try {
            Set<DataImport> removedDataImports = new HashSet<>();
            for (String exportId: exportsId) {
                removeDataExportInternal(exportId);

                String importId = exportToImport.remove(exportId);
                if (importId == null) {
                    throw new RuntimeException("Cannot find DataImport by DataExport id " + exportId);
                }
                importToExport.remove(importId);

                DataImport dataImport = removeDataImportInternal(importId);
                removedDataImports.add(dataImport);
            }
            return removedDataImports;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
