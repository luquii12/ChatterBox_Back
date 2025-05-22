-- Desactivar restricciones de FK --> ¡SOLO PARA DESARROLLO!
SET FOREIGN_KEY_CHECKS = 0;

# Eliminar tablas si existen
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS grupos;
DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS mensajes;
DROP TABLE IF EXISTS usuarios_grupos;

-- Activar restricciones de FK de nuevo --> ¡SOLO PARA DESARROLLO!
SET FOREIGN_KEY_CHECKS = 1;

# Tabla usuarios
CREATE TABLE usuarios
(
    id_usuario       BIGINT AUTO_INCREMENT PRIMARY KEY,
    apodo            VARCHAR(255) UNIQUE NOT NULL,
    nombre_usuario   VARCHAR(255)        NOT NULL,                                  -- Por ahora no lo vamos a usar. En el futuro puede que sí
    email            VARCHAR(255) UNIQUE NOT NULL,
    hash_password    VARCHAR(255)        NOT NULL,
    es_admin_general BOOLEAN             NOT NULL DEFAULT FALSE,
    foto_perfil      VARCHAR(255)        NOT NULL DEFAULT 'foto_perfil_default.png' -- Opcional para el futuro: se guarda solo la url de donde se encuentra
);

# Tabla grupos
CREATE TABLE grupos
(
    id_grupo           BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_creador BIGINT       NOT NULL,
    descripcion        VARCHAR(255),
    nombre_grupo       VARCHAR(255) NOT NULL,
    es_privado         BOOLEAN      NOT NULL DEFAULT FALSE,
    foto_grupo         VARCHAR(255) NOT NULL,
    CONSTRAINT fk_grupos_usuarios_creadores FOREIGN KEY (id_usuario_creador) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
);

-- Un mismo usuario no puede repetir el nombre del grupo
CREATE UNIQUE INDEX idx_grupos_nombre_usuario ON grupos (nombre_grupo, id_usuario_creador);

-- Índice para mejorar rendimiento al listar grupos creados por un usuario
CREATE INDEX idx_grupos_id_usuario_creador ON grupos (id_usuario_creador);


# Tabla chats
CREATE TABLE chats
(
    id_chat        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_grupo       BIGINT       NOT NULL,
    nombre_chat    VARCHAR(255) NOT NULL,
    fecha_creacion DATETIME     NOT NULL,
    CONSTRAINT fk_chat_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo) ON DELETE CASCADE
);

CREATE INDEX idx_chats_id_grupo ON chats (id_grupo);

-- No pueden existir chats con el mismo nombre en un grupo
# CREATE UNIQUE INDEX idx_chats_nombre_grupo ON chats (nombre_chat, id_grupo);

-- Si tenéis el último índice hay que borrarlo:
DROP INDEX idx_chats_nombre_grupo ON chats;

# Tabla mensajes
CREATE TABLE mensajes
(
    id_mensaje BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT      NOT NULL,
    id_chat    BIGINT      NOT NULL,
    hora_envio DATETIME(3) NOT NULL,
    contenido  TEXT        NOT NULL,
    CONSTRAINT fk_mensajes_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_chats FOREIGN KEY (id_chat) REFERENCES chats (id_chat) ON DELETE CASCADE
);

CREATE INDEX idx_mensajes_id_usuario ON mensajes (id_usuario);

CREATE INDEX idx_mensajes_id_chat_hora_envio ON mensajes (id_chat, hora_envio);


# Tabla usuarios_grupos
CREATE TABLE usuarios_grupos
(
    id_usuario        BIGINT   NOT NULL,
    id_grupo          BIGINT   NOT NULL,
    es_admin_grupo    BOOLEAN  NOT NULL DEFAULT FALSE,
    fecha_inscripcion DATETIME NOT NULL,
    PRIMARY KEY (id_usuario, id_grupo),
    CONSTRAINT fk_usuarios_grupos_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_grupos_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo) ON DELETE CASCADE
);

CREATE INDEX idx_usuarios_grupos_id_usuario ON usuarios_grupos (id_usuario);

CREATE INDEX idx_usuarios_grupos_id_grupo ON usuarios_grupos (id_grupo);
