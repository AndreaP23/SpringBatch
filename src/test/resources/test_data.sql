-- Cancella tutte le tabelle se esistono già
DROP TABLE IF EXISTS prenotazione;
DROP TABLE IF EXISTS veicolo;
DROP TABLE IF EXISTS "user";

-- Crea di nuovo le tabelle
CREATE TABLE "user" (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nome VARCHAR(255),
                        cognome VARCHAR(255),
                        email VARCHAR(255) UNIQUE,
                        password VARCHAR(255),
                        ruolo_id INT
);

CREATE TABLE veicolo (
                         veicolo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         marca VARCHAR(255),
                         modello VARCHAR(255),
                         anno INT,
                         targa VARCHAR(255) UNIQUE,
                         disponibilita INT
);

CREATE TABLE prenotazione (
                              prenotazione_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT,
                              veicolo_id BIGINT,
                              data_prenotazione DATE,
                              data_inizio DATE,
                              data_fine DATE,
                              note VARCHAR(255),
                              FOREIGN KEY (user_id) REFERENCES "user"(user_id),
                              FOREIGN KEY (veicolo_id) REFERENCES veicolo(veicolo_id)
);

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

-- Inserisci prenotazioni
INSERT INTO prenotazione (user_id, veicolo_id, data_prenotazione, data_inizio, data_fine, note) VALUES
                                                                                                    (1, 1, '2024-11-01', '2024-11-05', '2024-11-10', 'Prenotazione per viaggio di lavoro'),
                                                                                                    (2, 3, '2024-11-02', '2024-11-06', '2024-11-09', 'Visita parenti'),
                                                                                                    (3, 5, '2024-11-03', '2024-11-07', '2024-11-08', 'Gita fuori porta'),
                                                                                                    (4, 7, '2024-11-04', '2024-11-08', '2024-11-11', 'Utilizzo personale'),
                                                                                                    (5, 2, '2024-11-05', '2024-11-09', '2024-11-12', 'Viaggio in città'),
                                                                                                    (6, 6, '2024-11-06', '2024-11-10', '2024-11-13', 'Vacanza con amici'),
                                                                                                    (7, 8, '2024-11-07', '2024-11-11', '2024-11-14', 'Viaggio aziendale'),
                                                                                                    (8, 4, '2024-11-08', '2024-11-12', '2024-11-15', 'Weekend fuori città'),
                                                                                                    (9, 10, '2024-11-09', '2024-11-13', '2024-11-16', 'Viaggio esplorativo'),
                                                                                                    (10, 9, '2024-11-10', '2024-11-14', '2024-11-17', 'Vacanza familiare');