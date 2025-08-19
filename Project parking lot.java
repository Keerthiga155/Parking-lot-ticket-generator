package Parking_lot;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Scanner;

public class ParkingTicketSystem {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_lot";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "keerthiga@155";
    private static final double RATE_PER_HOUR = 20.0; // ₹20 per hour

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("1. Generate Ticket");
            System.out.println("2. Exit Vehicle & Calculate Fee");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 1) {
                System.out.print("Enter Vehicle Number: ");
                String vehicleNumber = sc.nextLine();

                System.out.print("Enter Parking Slot: ");
                String parkingSlot = sc.nextLine();
                
                System.out.print("Enter name: ");
                String customer_name = sc.nextLine();
                
                int ticketId = generateTicket(vehicleNumber, parkingSlot,customer_name);
                System.out.println(" Ticket Generated! Ticket ID: " + ticketId);
                fetchTicket(ticketId);

            } else if (choice == 2) {
                System.out.print("Enter Ticket ID: ");
                int ticketId = sc.nextInt();
                calculateAndUpdateFee(ticketId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int generateTicket(String vehicleNumber, String parkingSlot,String customer_name) {
        int ticketId = -1;
        String sql = "INSERT INTO tickets (vehicle_number, parking_slot,customer_name) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vehicleNumber);
            ps.setString(2, parkingSlot);
            ps.setString(3, customer_name);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ticketId = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ticketId;
    }

    public static void fetchTicket(int ticketId) {
        String sql = "SELECT * FROM tickets WHERE ticket_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                System.out.println("Vehicle Number: " + rs.getString("vehicle_number"));
                System.out.println("name " + rs.getString("customer_name"));
                System.out.println("Entry Time: " + rs.getTimestamp("entry_time"));
                System.out.println("Parking Slot: " + rs.getString("parking_slot"));
            } else {
                System.out.println(" Ticket not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void calculateAndUpdateFee(int ticketId) {
        String fetchSql = "SELECT entry_time FROM tickets WHERE ticket_id = ?";
        String updateSql = "UPDATE tickets SET exit_time = ?, fee = ? WHERE ticket_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement fetchPs = conn.prepareStatement(fetchSql);
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

            // Get entry time
            fetchPs.setInt(1, ticketId);
            ResultSet rs = fetchPs.executeQuery();

            if (rs.next()) {
                Timestamp entryTs = rs.getTimestamp("entry_time");
                LocalDateTime entryTime = entryTs.toLocalDateTime();
                LocalDateTime exitTime = LocalDateTime.now();

                long minutes = Duration.between(entryTime, exitTime).toMinutes();
                long hours = (long) Math.ceil(minutes / 60.0);
                double fee = hours * RATE_PER_HOUR;

                // Update DB
                updatePs.setTimestamp(1, Timestamp.valueOf(exitTime));
                updatePs.setDouble(2, fee);
                updatePs.setInt(3, ticketId);
                updatePs.executeUpdate();

                System.out.println(" Vehicle exited.");
                System.out.println("Total Time Parked: " + minutes + " minutes");
                System.out.println("Fee: ₹" + fee);

            } else {
                System.out.println(" Ticket not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
