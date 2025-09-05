package dev.guilhermeluan.todo_list.config;

import dev.guilhermeluan.todo_list.utils.ZonedDateTimeReadConverter;
import dev.guilhermeluan.todo_list.utils.ZonedDateTimeWriteConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;

@Configuration
public class MyMongoConfiguration extends AbstractMongoClientConfiguration {
    @Override
    protected String getDatabaseName() {
        return "database";
    }

    @Override
    protected void configureConverters(MongoConverterConfigurationAdapter adapter) {
        adapter.registerConverter(new ZonedDateTimeReadConverter());
        adapter.registerConverter(new ZonedDateTimeWriteConverter());


    }
}
