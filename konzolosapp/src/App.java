import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class App {

    public static class Berles {
        private int uid;
        private int yachtid;
        private LocalDate startdate;
        private LocalDate enddate;
        private int dailyPrice;
        private String name;

        public Berles(int uid, int yachtid, LocalDate startdate, LocalDate enddate, int dailyPrice, String name) {
            this.uid = uid;
            this.yachtid = yachtid;
            this.startdate = startdate;
            this.enddate = enddate;
            this.dailyPrice = dailyPrice;
            this.name = name;
        }

        public int getUid() {
            return uid;
        }

        public int getYachtid() {
            return yachtid;
        }

        public LocalDate getStartdate() {
            return startdate;
        }

        public LocalDate getEnddate() {
            return enddate;
        }

        public int getDailyPrice() {
            return dailyPrice;
        }

        public String getName() {
            return name;
        }

        // Számított tulajdonság: teljes ár (napok száma * napi díj), kezdőnap is beleértve
        public long getTotalPrice() {
            long days = ChronoUnit.DAYS.between(startdate, enddate) + 1;
            return days * dailyPrice;
        }

        // Bérlés hossza napokban
        public long getDurationDays() {
            return ChronoUnit.DAYS.between(startdate, enddate) + 1;
        }
    }

    public static void main(String[] args) {
        List<Berles> berlesek = new ArrayList<>();

        // 1. Adatbetöltés
        try (BufferedReader br = new BufferedReader(new FileReader("yacht_berlesek_2024.csv"))) {
            String line = br.readLine(); // fejléc kihagyása
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                int uid = Integer.parseInt(parts[0]);
                int yachtid = Integer.parseInt(parts[1]);
                LocalDate startdate = LocalDate.parse(parts[2]);
                LocalDate enddate = LocalDate.parse(parts[3]);
                int dailyPrice = Integer.parseInt(parts[4]);
                String name = parts[5];

                berlesek.add(new Berles(uid, yachtid, startdate, enddate, dailyPrice, name));
            }
        } catch (IOException e) {
            System.err.println("Hiba a fájl beolvasásakor: " + e.getMessage());
            return;
        }

        Scanner sc = new Scanner(System.in);

        // 2. Havi bevétel kiszámítása
        int month = 0;
        do {
            System.out.print("Adjon meg egy hónapot (1-12): ");
            if (sc.hasNextInt()) {
                month = sc.nextInt();
            } else {
                sc.next(); // rossz input törlése
            }
        } while (month < 1 || month > 12);

        long monthlyRevenue = 0;
        for (Berles b : berlesek) {
            LocalDate start = b.getStartdate();
            LocalDate end = b.getEnddate();

            LocalDate monthStart = LocalDate.of(2024, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            if (!(end.isBefore(monthStart) || start.isAfter(monthEnd))) {
            monthlyRevenue += b.getTotalPrice();
            }
        }


        // 3. Teljes éves bevétel
        long totalRevenue = 0;
        for (Berles b : berlesek) {
            totalRevenue += b.getTotalPrice();
        }

        // 4. Legdrágább bérlés
        Berles maxBerles = Collections.max(berlesek, Comparator.comparingLong(Berles::getTotalPrice));

        // 5. Bérelhető yachtok száma (egyedi yachtid alapján)
        long uniqueYachts = berlesek.stream()
                .map(Berles::getYachtid)
                .distinct()
                .count();

        // 6. Leggyakrabban bérelt yacht (név alapján)
        Map<String, Long> yachtCount = berlesek.stream()
                .collect(Collectors.groupingBy(Berles::getName, Collectors.counting()));

        String mostRentedYacht = Collections.max(yachtCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        long mostRentedCount = yachtCount.get(mostRentedYacht);

        // 8. Átlagos bérlési időtartam napban
        double avgDuration = berlesek.stream()
                .mapToLong(Berles::getDurationDays)
                .average()
                .orElse(0.0);

        // Kiírás formázottan (ezer tagolással)
        System.out.printf("A(z) %d. hónap bevétele: %,d euró%n", month, monthlyRevenue);
        System.out.printf("A teljes 2024-es éves bevétel: %,d euró%n", totalRevenue);
        System.out.printf("A legdrágább bérlés az %s yacht volt, teljes ár: %,d euró%n", maxBerles.getName(), maxBerles.getTotalPrice());
        System.out.printf("Összesen %d különböző yachtot béreltek ki.%n", uniqueYachts);
        System.out.printf("A legtöbbször bérelt yacht: %s (%d bérlés)%n", mostRentedYacht, mostRentedCount);
        System.out.printf("Átlagos bérlési időtartam: %.2f nap%n", avgDuration);
    }
}
