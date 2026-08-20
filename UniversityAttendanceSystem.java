import java.io.*;
import java.util.*;
import java.time.*;

class User {
    String username, password, role;

    User(String u, String p, String r) {
        username = u;
        password = p;
        role = r;
    }
}

public class UniversityAttendanceSystem {

    static Scanner sc = new Scanner(System.in);

    static String staffFile = "staff.txt";
    static String attendanceFile = "attendance.txt";
    static String usersFile = "users.txt";

    static List<User> users = new ArrayList<>();

    static Map<Integer, String> staffNames = new HashMap<>();
    static Map<Integer, String> staffRoles = new HashMap<>();
    static Map<Integer, String> staffDept = new HashMap<>();
    static Map<Integer, String> staffCategory = new HashMap<>();

    // ---------------- LOAD USERS ----------------
    static void loadUsers() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(usersFile));
        String line;

        while ((line = br.readLine()) != null) {
            String[] d = line.split(",");
            if (d.length == 3)
                users.add(new User(d[0], d[1], d[2]));
        }
        br.close();
    }

    // ---------------- LOGIN (3 ATTEMPTS + LOCK) ----------------
    static User login() {

        System.out.println("===== LOGIN SYSTEM =====");

        int attempts = 3;

        while (attempts > 0) {

            System.out.print("Username: ");
            String u = sc.nextLine();

            System.out.print("Password: ");
            String p = sc.nextLine();

            boolean found = false;

            for (User user : users) {

                if (user.username.equals(u)) {
                    found = true;

                    if (user.password.equals(p)) {
                        System.out.println("Login Successful! Role: " + user.role);
                        return user;
                    } else {
                        attempts--;
                        System.out.println("Wrong password!");
                        System.out.println("Attempts left: " + attempts);
                        break;
                    }
                }
            }

            if (!found) {
                attempts--;
                System.out.println("User not found!");
                System.out.println("Attempts left: " + attempts);
            }

            if (attempts == 0) {
                System.out.println("🚫 SYSTEM LOCKED! Too many failed attempts.");
                return null;
            }

            System.out.println("----------------------");
        }

        return null;
    }

    // ---------------- LOAD STAFF ----------------
    static void loadStaff() throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(staffFile));
        String line;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d.length >= 5) {
                int id = Integer.parseInt(d[0]);

                staffNames.put(id, d[1]);
                staffRoles.put(id, d[2]);
                staffCategory.put(id, d[3]);
                staffDept.put(id, d[4]);
            }
        }

        br.close();
    }

    // ---------------- ADD STAFF ----------------
    static void addStaff() throws IOException {

        System.out.print("ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Role: ");
        String role = sc.nextLine();

        System.out.print("Category (core/sub): ");
        String category = sc.nextLine();

        System.out.print("Department: ");
        String dept = sc.nextLine();

        BufferedWriter bw = new BufferedWriter(new FileWriter(staffFile, true));
        bw.write(id + "," + name + "," + role + "," + category + "," + dept);
        bw.newLine();
        bw.close();

        System.out.println("Staff Added!");
    }

    // ---------------- CHECK IN ----------------
    static void checkIn() throws IOException {

        System.out.print("Staff ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        String date = LocalDate.now().toString();
        String time = LocalTime.now().withNano(0).toString();

        BufferedWriter bw = new BufferedWriter(new FileWriter(attendanceFile, true));
        bw.write(id + "," + date + "," + time + ",,0");
        bw.newLine();
        bw.close();

        System.out.println("Check-in done!");
    }

    // ---------------- CHECK OUT ----------------
    static void checkOut() throws IOException {

        System.out.print("Staff ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        String date = LocalDate.now().toString();
        String outTime = LocalTime.now().withNano(0).toString();

        File input = new File(attendanceFile);
        File temp = new File("temp.txt");

        BufferedReader br = new BufferedReader(new FileReader(input));
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

        String line;
        boolean found = false;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d.length >= 4 &&
                d[0].equals(String.valueOf(id)) &&
                d[1].equals(date) &&
                d[3].isEmpty()) {

                LocalTime in = LocalTime.parse(d[2]);
                LocalTime out = LocalTime.parse(outTime);

                double hours = Duration.between(in, out).toMinutes() / 60.0;

                bw.write(d[0] + "," + d[1] + "," + d[2] + "," + outTime + "," + hours);
                found = true;

            } else {
                bw.write(line);
            }

            bw.newLine();
        }

        br.close();
        bw.close();

        input.delete();
        temp.renameTo(input);

        if (found)
            System.out.println("Check-out done!");
        else
            System.out.println("No check-in found!");
    }

    // ---------------- DAILY REPORT ----------------
    static void dailyReport() throws IOException {

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.next();

        BufferedReader br = new BufferedReader(new FileReader(attendanceFile));

        System.out.println("\n===== DAILY REPORT =====");

        String line;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d.length >= 5 && d[1].equals(date)) {

                int id = Integer.parseInt(d[0]);

                System.out.println(
                    "ID: " + id +
                    " | Name: " + staffNames.getOrDefault(id, "Unknown") +
                    " | Role: " + staffRoles.getOrDefault(id, "Unknown") +
                    " | Dept: " + staffDept.getOrDefault(id, "Unknown") +
                    " | In: " + d[2] +
                    " | Out: " + d[3] +
                    " | Hours: " + d[4]
                );
            }
        }

        br.close();
    }

    // ---------------- MONTHLY REPORT ----------------
    static void monthlyReport() throws IOException {

        System.out.print("Enter Month (YYYY-MM): ");
        String month = sc.next();

        BufferedReader br = new BufferedReader(new FileReader(attendanceFile));

        System.out.println("\n===== MONTHLY REPORT =====");

        String line;
        double total = 0;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d.length >= 5 && d[1].startsWith(month)) {

                int id = Integer.parseInt(d[0]);
                double hrs = Double.parseDouble(d[4]);
                total += hrs;

                System.out.println(
                    "ID: " + id +
                    " | Name: " + staffNames.getOrDefault(id, "Unknown") +
                    " | Dept: " + staffDept.getOrDefault(id, "Unknown") +
                    " | Date: " + d[1] +
                    " | Hours: " + d[4]
                );
            }
        }

        System.out.println("\nTOTAL MONTH HOURS: " + total);

        br.close();
    }

    // ---------------- YEARLY REPORT ----------------
    static void yearlyReport() throws IOException {

        System.out.print("Enter Year (YYYY): ");
        String year = sc.next();

        BufferedReader br = new BufferedReader(new FileReader(attendanceFile));

        System.out.println("\n===== YEARLY REPORT =====");

        String line;
        double total = 0;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d.length >= 5 && d[1].startsWith(year)) {

                int id = Integer.parseInt(d[0]);
                double hrs = Double.parseDouble(d[4]);
                total += hrs;

                System.out.println(
                    "ID: " + id +
                    " | Name: " + staffNames.getOrDefault(id, "Unknown") +
                    " | Dept: " + staffDept.getOrDefault(id, "Unknown") +
                    " | Date: " + d[1] +
                    " | Hours: " + d[4]
                );
            }
        }

        System.out.println("\nTOTAL YEAR HOURS: " + total);

        br.close();
    }

    // ---------------- CHANGE PASSWORD ----------------
    static void changePassword() throws IOException {

        System.out.print("Username: ");
        String username = sc.next();

        System.out.print("Old Password: ");
        String oldPass = sc.next();

        System.out.print("New Password: ");
        String newPass = sc.next();

        File input = new File(usersFile);
        File temp = new File("temp_users.txt");

        BufferedReader br = new BufferedReader(new FileReader(input));
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

        String line;
        boolean updated = false;

        while ((line = br.readLine()) != null) {

            String[] d = line.split(",");

            if (d[0].equals(username) && d[1].equals(oldPass)) {
                bw.write(d[0] + "," + newPass + "," + d[2]);
                updated = true;
            } else {
                bw.write(line);
            }

            bw.newLine();
        }

        br.close();
        bw.close();

        input.delete();
        temp.renameTo(input);

        if (updated)
            System.out.println("Password changed!");
        else
            System.out.println("Invalid credentials!");
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) throws Exception {

        loadUsers();

        User user = login();

        if (user == null) return;

        loadStaff();

        while (true) {

            System.out.println("\n===== MENU =====");

            if (user.role.equals("admin")) {

                System.out.println("1. Add Staff");
                System.out.println("2. Check In");
                System.out.println("3. Check Out");
                System.out.println("4. Daily Report");
                System.out.println("5. Monthly Report");
                System.out.println("6. Yearly Report");
                System.out.println("7. Change Password");
                System.out.println("8. Exit");

                int c = sc.nextInt();
                sc.nextLine();

                switch (c) {
                    case 1 -> addStaff();
                    case 2 -> checkIn();
                    case 3 -> checkOut();
                    case 4 -> dailyReport();
                    case 5 -> monthlyReport();
                    case 6 -> yearlyReport();
                    case 7 -> changePassword();
                    case 8 -> System.exit(0);
                }

            } else {

                System.out.println("1. Check In");
                System.out.println("2. Check Out");
                System.out.println("3. Change Password");
                System.out.println("4. Exit");

                int c = sc.nextInt();
                sc.nextLine();

                switch (c) {
                    case 1 -> checkIn();
                    case 2 -> checkOut();
                    case 3 -> changePassword();
                    case 4 -> System.exit(0);
                }
            }
        }
    }
}