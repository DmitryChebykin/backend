package ru.region_stat.domain.entity.oneTimeRequest;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class ImportanceConverter implements AttributeConverter<Importance, String> {
    @Override
    public String convertToDatabaseColumn(Importance importance) {
        if (importance == null) {
            return null;
        }
        return importance.getName();
    }

    @Override
    public Importance convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(Importance.values())
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}