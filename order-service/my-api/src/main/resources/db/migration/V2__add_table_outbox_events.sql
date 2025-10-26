
    create table outbox_events (
        created_at datetime(6) not null,
        published_at datetime(6),
        id binary(16) not null,
        aggregate_id varchar(255) not null,
        aggregate_type varchar(255) not null,
        event_type varchar(255) not null,
        payload TEXT not null,
        primary key (id)
    ) engine=InnoDB;