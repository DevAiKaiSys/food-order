
    create table customers (
        created_at datetime(6) not null,
        id bigint not null auto_increment,
        updated_at datetime(6),
        phone varchar(20) not null,
        name varchar(100) not null,
        primary key (id)
    ) engine=InnoDB;

    create table order_items (
        price decimal(10,2) not null,
        quantity integer not null,
        subtotal decimal(10,2) not null,
        id bigint not null auto_increment,
        order_id bigint not null,
        menu_name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table orders (
        total_amount decimal(10,2) not null,
        created_at datetime(6) not null,
        customer_id bigint not null,
        id bigint not null auto_increment,
        updated_at datetime(6),
        span_id varchar(50),
        slip_id varchar(50) not null,
        status enum ('CANCELLED','COMPLETED','CONFIRMED','COOKING','DELIVERING','PENDING') not null,
        primary key (id)
    ) engine=InnoDB;

    alter table order_items 
       add constraint FKbioxgbv59vetrxe0ejfubep1w 
       foreign key (order_id) 
       references orders (id);

    alter table orders 
       add constraint FKpxtb8awmi0dk6smoh2vp1litg 
       foreign key (customer_id) 
       references customers (id);
