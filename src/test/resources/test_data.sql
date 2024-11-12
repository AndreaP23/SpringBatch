--DROP TABLE IF EXISTS prenotazione;
--DROP TABLE IF EXISTS veicolo;
--DROP TABLE IF EXISTS "user";

--CREATE TABLE "user" (
--                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
--                        nome VARCHAR(255),
--                        cognome VARCHAR(255),
--                        email VARCHAR(255) UNIQUE,
--                        password VARCHAR(255),
--                        ruolo_id INT
--);

--CREATE TABLE veicolo (
--                         veicolo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
--                         marca VARCHAR(255),
--                         modello VARCHAR(255),
--                         anno INT,
--                         targa VARCHAR(255) UNIQUE,
--                         disponibilita INT
--);

--CREATE TABLE prenotazione (
--                              prenotazione_id BIGINT AUTO_INCREMENT PRIMARY KEY,
--                              user_id BIGINT,
--                              veicolo_id BIGINT,
--                              data_prenotazione DATE,
--                              data_inizio DATE,
--                              data_fine DATE,
--                              note VARCHAR(255),
--                              FOREIGN KEY (user_id) REFERENCES "user"(user_id),
--                              FOREIGN KEY (veicolo_id) REFERENCES veicolo(veicolo_id)
--);

-- Inserisci utenti
INSERT INTO "user" (nome, cognome, email, password, ruolo_id) VALUES
                                                                  ('Mario', 'Rossi', 'mario@example.com', 'password', 1),
                                                                  ('Luca', 'Bianchi', 'luca@example.com', 'password', 2),
                                                                  ('Giulia', 'Verdi', 'giulia@example.com', 'password', 2),
                                                                  ('Andrea', 'Neri', 'andrea@example.com', 'password', 1),
                                                                  ('Elena', 'Gialli', 'elena@example.com', 'password', 2),
                                                                  ('Marco', 'Blu', 'marco@example.com', 'password', 1),
                                                                  ('Sofia', 'Arancio', 'sofia@example.com', 'password', 2),
                                                                  ('Alessandro', 'Rosa', 'alessandro@example.com', 'password', 1),
                                                                  ('Chiara', 'Marrone', 'chiara@example.com', 'password', 2),
                                                                  ('Fabio', 'Grigi', 'fabio@example.com', 'password', 1);

-- Inserisci veicoli
INSERT INTO veicolo (marca, modello, anno, targa, disponibilita) VALUES
                                                                     ('Fiat', 'Panda', 2020, 'AB123CD', 1),
                                                                     ('Volkswagen', 'Golf', 2019, 'EF456GH', 1),
                                                                     ('Toyota', 'Yaris', 2021, 'IJ789KL', 1),
                                                                     ('Renault', 'Clio', 2018, 'MN123OP', 1),
                                                                     ('Ford', 'Focus', 2017, 'QR456ST', 1),
                                                                     ('BMW', 'Serie 1', 2022, 'UV789WX', 1),
                                                                     ('Audi', 'A3', 2020, 'YZ123AA', 1),
                                                                     ('Mercedes', 'Classe A', 2019, 'BB456CC', 1),
                                                                     ('Peugeot', '208', 2021, 'DD789EE', 1),
                                                                     ('Honda', 'Civic', 2022, 'FF123GG', 1);