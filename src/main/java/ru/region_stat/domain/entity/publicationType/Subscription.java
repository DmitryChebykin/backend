package ru.region_stat.domain.entity.publicationType;

import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum Subscription {
    PUBLICATION("Статистическое издание"),
    REQUEST("Постоянный запрос");

    private final String name;

    Subscription(String name) {
        this.name = name;
    }

    public static Subscription of(String name) {

        if (name == null) {
            return null;
        }

        return Stream.of(Subscription.values())
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String toString() {
        return name;
    }
}