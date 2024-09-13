package org.eventplanner.qualifications.adapter.filesystem;

import org.apache.commons.io.FileUtils;
import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.utils.FileSystemJsonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Repository
public class QualificationFileSystemRepository implements QualificationRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final FileSystemJsonRepository<QualificationJsonEntity> fs;

    public QualificationFileSystemRepository(
        @Value("${custom.data-directory}") String dataDirectory,
        ResourceLoader resourceLoader
    ) {
        var dir = new File(dataDirectory + "/qualifications");
        if (!dir.exists()) {
            try {
                var resource = resourceLoader.getResource("classpath:data/qualifications");
                if (resource.exists()) {
                    var sourceDir = resource.getFile();
                    FileUtils.copyDirectory(sourceDir, dir);
                }
            } catch (IOException e) {
                log.error("Failed to copy bundled qualifications into data directory", e);
            }
        }
        this.fs = new FileSystemJsonRepository<>(QualificationJsonEntity.class, dir);
    }

    @Override
    public List<Qualification> findAll() {
        return fs.findAll().stream().map(QualificationJsonEntity::toDomain).toList();
    }
}
