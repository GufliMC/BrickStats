package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;
import com.guflimc.brick.orm.ebean.database.EbeanDatabaseContext;
import com.guflimc.brick.orm.ebean.database.EbeanMigrations;
import com.guflimc.brick.stats.common.domain.DActor;
import com.guflimc.brick.stats.common.domain.DRecord;
import io.ebean.annotation.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

public class BrickStatsDatabaseContext extends EbeanDatabaseContext {

    private final static String DATASOURCE_NAME = "BrickStats";

    public BrickStatsDatabaseContext(EbeanConfig config) {
        super(config, DATASOURCE_NAME);
    }

    public BrickStatsDatabaseContext(EbeanConfig config, int poolSize) {
        super(config, DATASOURCE_NAME, poolSize);
    }

    @Override
    protected Class<?>[] applicableClasses() {
        return APPLICABLE_CLASSES;
    }

    private static final Class<?>[] APPLICABLE_CLASSES = new Class[]{
            DRecord.class,

            DActor.ActorPK.class,
            DActor.class
    };

    public static void main(String[] args) throws IOException, SQLException {
        EbeanMigrations generator = new EbeanMigrations(
                DATASOURCE_NAME,
                Path.of("BrickStats/common/src/main/resources"),
                Platform.H2, Platform.MYSQL
        );
        Arrays.stream(APPLICABLE_CLASSES).forEach(generator::addClass);
        generator.generate();
    }

}
