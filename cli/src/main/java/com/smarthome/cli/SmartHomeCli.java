package com.smarthome.cli;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;

public class SmartHomeCli {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static String jwtToken = null;
    private static String currentUserEmail = "admin@test.com"; // Default for demo

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Home CLI");

        while (true) {
            if (jwtToken == null) {
                System.out.println("\n1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        login(scanner);
                        break;
                    case 2:
                        register(scanner);
                        break;
                    case 3:
                        System.exit(0);
                    default:
                        System.out.println("Invalid option");
                }
            } else {
                System.out.println("\n--- Dashboard ---");
                System.out.println("1. List Devices");
                System.out.println("2. Add Device");
                System.out.println("3. Toggle Device");
                System.out.println("4. Logout");
                System.out.println("5. Exit");
                System.out.print("Choose option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        listDevices();
                        break;
                    case 2:
                        addDevice(scanner);
                        break;
                    case 3:
                        toggleDevice(scanner);
                        break;
                    case 4:
                        jwtToken = null;
                        break;
                    case 5:
                        System.exit(0);
                    default:
                        System.out.println("Invalid option");
                }
            }
        }
    }

    private static void login(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String json = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AuthResponse auth = gson.fromJson(response.body(), AuthResponse.class);
                jwtToken = auth.token;
                System.out.println("Login Successful!");
            } else {
                System.out.println("Login Failed: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void register(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String json = String.format("{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"role\":\"HOMEOWNER\"}",
                name, email, password);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response: " + response.body());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listDevices() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/devices"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Device> devices = gson.fromJson(response.body(), new TypeToken<List<Device>>() {
                }.getType());
                System.out.printf("%-5s %-20s %-10s %-10s%n", "ID", "Name", "Type", "Status");
                System.out.println("------------------------------------------------");
                for (Device d : devices) {
                    System.out.printf("%-5d %-20s %-10s %-10s%n", d.id, d.name, d.type, d.status ? "ON" : "OFF");
                }
            } else {
                System.out.println("Failed to list devices: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addDevice(Scanner scanner) {
        System.out.print("Device Name: ");
        String name = scanner.nextLine();
        System.out.print("Type (LIGHT/AC/FAN): ");
        String type = scanner.nextLine();
        System.out.print("Power Rating (Watts): ");
        double power = scanner.nextDouble();
        scanner.nextLine();

        String json = String.format("{\"name\":\"%s\", \"type\":\"%s\", \"powerRating\":%.2f}", name, type, power);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/devices"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Device added successfully.");
            } else {
                System.out.println("Failed to add device.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void toggleDevice(Scanner scanner) {
        System.out.print("Enter Device ID: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/devices/" + id + "/status"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Device toggled successfully.");
            } else {
                System.out.println("Failed to toggle device.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Inner classes for GSON
    static class AuthResponse {
        String token;
    }

    static class Device {
        long id;
        String name;
        String type;
        boolean status;
    }
}
