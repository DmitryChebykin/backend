package ru.region_stat.domain.entity.oneTimeRequest;

import lombok.Getter;
import java.util.stream.Stream;

@Getter
public enum Importance {
    HIGH("(1) Высокая"),
    REGULAR("(2) Обычная"),
    LOW("(3) Низкая");

    private final String name;

    Importance(String name) {
        this.name = name;
    }

    public static Importance of(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(Importance.values())
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String toString() {
        return name();
    }
}