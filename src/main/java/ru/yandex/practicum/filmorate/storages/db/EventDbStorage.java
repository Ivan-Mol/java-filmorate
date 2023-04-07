package ru.yandex.practicum.filmorate.storages.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storages.EventStorage;

import java.util.ArrayList;
import java.util.List;

@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        log.debug("/addEvent");
        String sql = "INSERT INTO events (event_type, operation, user_id, entity_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                sql, event.getEventType().name(), event.getOperation().name(), event.getUserId(), event.getEntityId());
    }

    @Override
    public List<Event> getUserEvents(long userId) {
        log.debug("/getUserEvents");
        String sql = "SELECT * FROM events AS e WHERE e.user_id = " + userId + " ORDER BY e.timestamp";
        return jdbcTemplate.query(sql, rs -> {
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getLong("id"));
                event.setTimestamp(rs.getTimestamp("timestamp").getTime());
                event.setEventType(EventType.valueOf(rs.getString("event_type")));
                event.setOperation(OperationType.valueOf(rs.getString("operation")));
                event.setUserId(rs.getLong("user_id"));
                event.setEntityId(rs.getLong("entity_id"));
                events.add(event);
            }
            return events;
        });
    }
}