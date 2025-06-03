-- Usuarios
INSERT INTO usuarios (apodo, nombre_usuario, email, hash_password, es_admin_general, foto_perfil)
VALUES ('lucasDev', 'Lucas', 'lucas@example.com', '$2a$10$q/pAQNTBCg.4tUfYVrb0xe3tKULxJAQ3xcMTXvnjSbvU4HHcTS0Dq', TRUE,
        'foto_perfil_default.png'), -- Contraseña: 1234
       ('anaCoder', 'Ana', 'ana@example.com', '$2a$10$DEbzgJ6FSgCJaZEC52VZIu0deDUHtw/aOTp10PEtvMorw2bSiN6oC', FALSE,
        'foto_perfil_default.png'), -- Contraseña: 1234
       ('david123', 'David', 'david@example.com', '$2a$10$khWNrSTXaNM4IlaIJ3R7ZO3iFCOFz24CePFzo89M7QFGkOYXml4A2', FALSE,
        'foto_perfil_default.png');
-- Contraseña: 1234

-- Grupos
INSERT INTO grupos (id_usuario_creador, descripcion, nombre_grupo, es_privado, foto_grupo)
VALUES (1, 'Grupo de pruebas de Lucas', 'DevTalks', FALSE, NULL),
       (2, 'Grupo de Ana sobre IA', 'AI Lovers', TRUE, NULL);

-- Chats
INSERT INTO chats (id_grupo, nombre_chat, fecha_creacion)
VALUES (1, 'general', NOW()),
       (1, 'backend', DATE_ADD(NOW(), INTERVAL 1 MINUTE)),
       (2, 'noticias', DATE_ADD(NOW(), INTERVAL 2 MINUTE)),
       (2, 'proyectos', DATE_ADD(NOW(), INTERVAL 3 MINUTE));

-- Usuarios en grupos
INSERT INTO usuarios_grupos (id_usuario, id_grupo, es_admin_grupo, fecha_inscripcion)
VALUES (1, 1, TRUE, NOW()),                               -- Lucas admin en DevTalks
       (2, 1, FALSE, DATE_ADD(NOW(), INTERVAL 1 MINUTE)), -- Ana miembro en DevTalks
       (2, 2, TRUE, DATE_ADD(NOW(), INTERVAL 2 MINUTE)),  -- Ana admin en AI Lovers
       (3, 2, FALSE, DATE_ADD(NOW(), INTERVAL 3 MINUTE));
-- David miembro en AI Lovers

-- Mensajes
INSERT INTO mensajes (id_usuario, id_chat, hora_envio, contenido)
VALUES (1, 1, NOW(3), '¡Hola a todos en DevTalks!'),
       (2, 1, DATE_ADD(NOW(3), INTERVAL 1 MINUTE), '¡Buenas Lucas!'),
       (1, 2, DATE_ADD(NOW(3), INTERVAL 2 MINUTE), '¿Alguien sabe algo de Spring Boot?'),
       (2, 3, DATE_ADD(NOW(3), INTERVAL 3 MINUTE), 'Últimas noticias sobre IA: GPT-5 está en camino.'),
       (3, 4, DATE_ADD(NOW(3), INTERVAL 4 MINUTE), 'Estoy trabajando en un proyecto de visión artificial.');

-- Eliminar datos en orden inverso a las dependencias
# DELETE
# FROM mensajes;
# DELETE
# FROM usuarios_grupos;
# DELETE
# FROM chats;
# DELETE
# FROM grupos;
# DELETE
# FROM usuarios;