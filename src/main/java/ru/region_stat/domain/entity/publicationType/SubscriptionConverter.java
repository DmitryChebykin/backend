package ru.region_stat.domain.entity.publicationType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class SubscriptionConverter implements AttributeConverter<Subscription, String> {
    @Override
    public String convertToDatabaseColumn(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        return subscription.getName();
    }

    @Override
    public Subscription convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(Subscription.values())
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}