#  Repasar las tablas y modificar lo necesario
/*
Mejorar la BD: ¿Nulos y no nulos? ¿Campos Unique?
(Para pasar de Hibernate a MySQL)
https://chatgpt.com/c/67f38b12-4190-800a-bb9c-938ef0557260
*/
create table grupo
(
    id_grupo     bigint not null auto_increment,
    descripcion  varchar(255),
    nombre_grupo varchar(255),
    primary key (id_grupo)
) engine = InnoDB;

create table mensaje
(
    hora_envio datetime(6),
    id_grupo   bigint,
    id_mensaje bigint not null auto_increment,
    id_usuario bigint,
    contenido  varchar(255),
    primary key (id_mensaje)
) engine = InnoDB;

create table usuario
(
    id_usuario     bigint not null auto_increment,
    apellidos      varchar(255),
    email          varchar(255),
    hash_password  varchar(255),
    nombre_usuario varchar(255),
    primary key (id_usuario)
) engine = InnoDB;

create table usuario_grupo
(
    es_admin          bit    not null,
    fecha_inscripcion date,
    id_grupo          bigint not null,
    id_usuario        bigint not null,
    primary key (id_grupo, id_usuario)
) engine = InnoDB;

alter table mensaje
    add constraint FKje73ixodwnt01r70qqvvm41k9 foreign key (id_grupo) references grupo (id_grupo);

alter table mensaje
    add constraint FKdic7ewqu7vimup6dp2hbu8x5e foreign key (id_usuario) references usuario (id_usuario);

alter table usuario_grupo
    add constraint FKcu6om65mvqr6ct95ijgqgx7ww foreign key (id_grupo) references grupo (id_grupo);

alter table usuario_grupo
    add constraint FK9huj1upwjyabwkwnpnhnernnu foreign key (id_usuario) references usuario (id_usuario);

create table grupo
(
    id_grupo     bigint not null auto_increment,
    descripcion  varchar(255),
    nombre_grupo varchar(255),
    primary key (id_grupo)
) engine = InnoDB;

create table mensaje
(
    hora_envio datetime(6),
    id_grupo   bigint,
    id_mensaje bigint not null auto_increment,
    id_usuario bigint,
    contenido  varchar(255),
    primary key (id_mensaje)
) engine = InnoDB;

create table usuario
(
    id_usuario     bigint not null auto_increment,
    apellidos      varchar(255),
    email          varchar(255),
    hash_password  varchar(255),
    nombre_usuario varchar(255),
    primary key (id_usuario)
) engine = InnoDB;

create table usuario_grupo
(
    es_admin          bit    not null,
    fecha_inscripcion date,
    id_grupo          bigint not null,
    id_usuario        bigint not null,
    primary key (id_grupo, id_usuario)
) engine = InnoDB;

alter table mensaje
    add constraint FKje73ixodwnt01r70qqvvm41k9 foreign key (id_grupo) references grupo (id_grupo);

alter table mensaje
    add constraint FKdic7ewqu7vimup6dp2hbu8x5e foreign key (id_usuario) references usuario (id_usuario);

alter table usuario_grupo
    add constraint FKcu6om65mvqr6ct95ijgqgx7ww foreign key (id_grupo) references grupo (id_grupo);

alter table usuario_grupo
    add constraint FK9huj1upwjyabwkwnpnhnernnu foreign key (id_usuario) references usuario (id_usuario);
