CREATE DATABASE parking_lot;

USE parking_lot;
CREATE TABLE tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL,
    entry_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parking_slot VARCHAR(10) NOT NULL
);
ALTER TABLE tickets
ADD COLUMN exit_time TIMESTAMP NULL,
ADD COLUMN fee DECIMAL(10,2) NULL;

ALTER TABLE tickets
ADD COLUMN customer_name varchar(30);
select*from tickets;
