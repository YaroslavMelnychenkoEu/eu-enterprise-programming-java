import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/* ===================== ВИНЯТКИ ===================== */
class InvalidDataException extends RuntimeException {
    public InvalidDataException(String msg) { super(msg); }
}
class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String msg) { super(msg); }
}
class TripRegistrationException extends RuntimeException {
    public TripRegistrationException(String msg) { super(msg); }
}

/* ===================== МОДЕЛІ ТА ДОВІДНИКИ ===================== */
enum TechStatus { OK, SERVICE_DUE, REPAIR_NEEDED }

class Trip {
    private final LocalDate date;
    private final double distanceKm;
    private final String driver;
    private final double fuelPricePerL;

    public Trip(LocalDate date, double distanceKm, String driver, double fuelPricePerL) {
        if (date == null) throw new InvalidDataException("Дата поїздки не може бути null");
        if (distanceKm <= 0) throw new InvalidDataException("Дистанція має бути > 0");
        if (driver == null || driver.isBlank()) throw new InvalidDataException("Ім'я водія не може бути порожнім");
        if (fuelPricePerL <= 0) throw new InvalidDataException("Ціна пального має бути > 0");
        this.date = date;
        this.distanceKm = distanceKm;
        this.driver = driver.trim();
        this.fuelPricePerL = fuelPricePerL;
    }

    public LocalDate getDate() { return date; }
    public double getDistanceKm() { return distanceKm; }
    public String getDriver() { return driver; }
    public double getFuelPricePerL() { return fuelPricePerL; }

    @Override public String toString() {
        return String.format("Trip{%s, %.1f км, %s, fuel=%.2f}",
                date, distanceKm, driver, fuelPricePerL);
    }
}

class Repair {
    private final LocalDate date;
    private final String description;
    private final double cost;
    private final boolean critical; // якщо критичний — ТЗ непридатне до завершення ремонту

    public Repair(LocalDate date, String description, double cost, boolean critical) {
        if (date == null) throw new InvalidDataException("Дата ремонту не може бути null");
        if (description == null || description.isBlank()) throw new InvalidDataException("Опис ремонту не може бути порожнім");
        if (cost < 0) throw new InvalidDataException("Вартість ремонту не може бути від'ємною");
        this.date = date;
        this.description = description.trim();
        this.cost = cost;
        this.critical = critical;
    }

    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public double getCost() { return cost; }
    public boolean isCritical() { return critical; }

    @Override public String toString() {
        return String.format("Repair{%s, '%s', cost=%.2f, critical=%s}",
                date, description, cost, critical ? "YES" : "no");
    }
}

/* ===================== АБСТРАКТНА МОДЕЛЬ ТЗ ===================== */
abstract class Vehicle {
    private final UUID id;
    private final String model;
    private final int year;
    private final double purchasePrice;
    private final double baseFuelPer100km;     // л/100км (базова витрата)
    private final double baseMaintPerKm;       // $/км (базове ТО)
    private double odometerKm;
    private double lastServiceOdoKm;           // пробіг на момент останнього ТО
    private LocalDate lastServiceDate;         // дата останнього ТО
    private final List<Trip> trips = new ArrayList<>();
    private final List<Repair> repairs = new ArrayList<>();
    private boolean hasActiveCriticalRepair = false;

    protected Vehicle(String model, int year, double purchasePrice,
                      double baseFuelPer100km, double baseMaintPerKm, double odometerKm) {
        if (model == null || model.isBlank()) throw new InvalidDataException("Модель не може бути порожньою");
        if (year < 1980 || year > LocalDate.now().getYear()) throw new InvalidDataException("Некоректний рік випуску");
        if (purchasePrice <= 0) throw new InvalidDataException("Ціна покупки має бути > 0");
        if (baseFuelPer100km <= 0) throw new InvalidDataException("Базова витрата пального має бути > 0");
        if (baseMaintPerKm < 0) throw new InvalidDataException("Вартість ТО за км не може бути від'ємною");
        if (odometerKm < 0) throw new InvalidDataException("Пробіг не може бути від'ємним");
        this.id = UUID.randomUUID();
        this.model = model.trim();
        this.year = year;
        this.purchasePrice = purchasePrice;
        this.baseFuelPer100km = baseFuelPer100km;
        this.baseMaintPerKm = baseMaintPerKm;
        this.odometerKm = odometerKm;
        this.lastServiceOdoKm = odometerKm;
        this.lastServiceDate = LocalDate.now();
    }

    /* ---- Абстрактні/перевизначувані параметри типу ТЗ ---- */
    protected abstract double fuelTypeCoeff();        // коеф. типу для витрати палива
    protected abstract double maintenanceTypeCoeff(); // коеф. типу для ТО
    protected abstract int depreciationYears();       // строк амортизації в роках

    /* ---- Розрахунки ---- */
    public double fuelUsedFor(double distanceKm) {
        return (baseFuelPer100km * fuelTypeCoeff()) * distanceKm / 100.0;
    }

    public double fuelCostFor(double distanceKm, double fuelPricePerL) {
        if (fuelPricePerL <= 0) throw new InvalidDataException("Ціна пального має бути > 0");
        return fuelUsedFor(distanceKm) * fuelPricePerL;
    }

    public double maintenanceCostFor(double distanceKm) {
        return baseMaintPerKm * maintenanceTypeCoeff() * distanceKm;
    }

    public TechStatus checkTechnicalStatus() {
        if (hasActiveCriticalRepair) return TechStatus.REPAIR_NEEDED;
        double kmSinceService = odometerKm - lastServiceOdoKm;
        Period sinceDate = Period.between(lastServiceDate, LocalDate.now());
        boolean dueByKm = kmSinceService >= 15000; // ТО кожні 15 тис. км
        boolean dueByTime = sinceDate.getMonths() + sinceDate.getYears() * 12 >= 12; // або раз на рік
        return (dueByKm || dueByTime) ? TechStatus.SERVICE_DUE : TechStatus.OK;
    }

    /** Пряма амортизація до ліквідаційної вартості 20% від ціни покупки */
    public double depreciationValue(LocalDate asOf) {
        if (asOf == null) asOf = LocalDate.now();
        int ageYears = Math.max(0, asOf.getYear() - year);
        int life = depreciationYears();
        double salvage = purchasePrice * 0.20;
        double annual = (purchasePrice - salvage) / life;
        double depreciated = Math.min(ageYears, life) * annual;
        double book = Math.max(salvage, purchasePrice - depreciated);
        return book;
    }

    /* ---- Операції ---- */
    public void registerTrip(Trip trip) {
        if (trip.getDate().isAfter(LocalDate.now().plusDays(1)))
            throw new TripRegistrationException("Дата поїздки не може бути з майбутнього");
        if (checkTechnicalStatus() == TechStatus.REPAIR_NEEDED)
            throw new TripRegistrationException("ТЗ має активний критичний ремонт — поїздка заборонена");
        trips.add(trip);
        odometerKm += trip.getDistanceKm();
    }

    public void completeService(LocalDate date) {
        if (date == null) date = LocalDate.now();
        lastServiceDate = date;
        lastServiceOdoKm = odometerKm;
    }

    public void addRepair(Repair r) {
        repairs.add(r);
        if (r.isCritical()) hasActiveCriticalRepair = true;
    }

    public void closeCriticalRepairs() {
        hasActiveCriticalRepair = false;
    }

    /* ---- Геттери ---- */
    public UUID getId() { return id; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getOdometerKm() { return odometerKm; }
    public List<Trip> getTrips() { return Collections.unmodifiableList(trips); }
    public List<Repair> getRepairs() { return Collections.unmodifiableList(repairs); }

    @Override public String toString() {
        return String.format("%s{id=%s, model='%s', year=%d, odo=%.0f км, status=%s, book=%.2f}",
                getClass().getSimpleName(), id.toString().substring(0,8), model, year,
                odometerKm, checkTechnicalStatus(), depreciationValue(LocalDate.now()));
    }
}

/* ===================== ТИПИ ТЗ ===================== */
class Car extends Vehicle {
    public Car(String model, int year, double purchasePrice,
               double baseFuelPer100km, double baseMaintPerKm, double odometerKm) {
        super(model, year, purchasePrice, baseFuelPer100km, baseMaintPerKm, odometerKm);
    }
    @Override protected double fuelTypeCoeff() { return 1.0; }
    @Override protected double maintenanceTypeCoeff() { return 1.0; }
    @Override protected int depreciationYears() { return 7; }
}

class Truck extends Vehicle {
    public Truck(String model, int year, double purchasePrice,
                 double baseFuelPer100km, double baseMaintPerKm, double odometerKm) {
        super(model, year, purchasePrice, baseFuelPer100km, baseMaintPerKm, odometerKm);
    }
    @Override protected double fuelTypeCoeff() { return 1.6; }
    @Override protected double maintenanceTypeCoeff() { return 1.8; }
    @Override protected int depreciationYears() { return 10; }
}

class Bus extends Vehicle {
    public Bus(String model, int year, double purchasePrice,
               double baseFuelPer100km, double baseMaintPerKm, double odometerKm) {
        super(model, year, purchasePrice, baseFuelPer100km, baseMaintPerKm, odometerKm);
    }
    @Override protected double fuelTypeCoeff() { return 1.4; }
    @Override protected double maintenanceTypeCoeff() { return 1.5; }
    @Override protected int depreciationYears() { return 12; }
}

/* ===================== КЕРУВАННЯ АВТОПАРКОМ ===================== */
class Fleet {
    private final Map<UUID, Vehicle> vehicles = new LinkedHashMap<>();

    public void addVehicle(Vehicle v) {
        vehicles.put(v.getId(), v);
    }

    public Vehicle getVehicle(UUID id) {
        Vehicle v = vehicles.get(id);
        if (v == null) throw new VehicleNotFoundException("ТЗ не знайдено: " + id);
        return v;
    }

    public List<Vehicle> listAll() {
        return new ArrayList<>(vehicles.values());
    }

    public List<Vehicle> findByType(Class<? extends Vehicle> type) {
        return vehicles.values().stream().filter(type::isInstance).collect(Collectors.toList());
    }

    public void registerTrip(UUID vehicleId, Trip trip) {
        getVehicle(vehicleId).registerTrip(trip);
    }

    public void addRepair(UUID vehicleId, Repair repair) {
        getVehicle(vehicleId).addRepair(repair);
    }

    public void completeService(UUID vehicleId, LocalDate date) {
        getVehicle(vehicleId).completeService(date);
    }

    public void closeCriticalRepairs(UUID vehicleId) {
        getVehicle(vehicleId).closeCriticalRepairs();
    }

    public double estimateTripFuelCost(UUID vehicleId, double distanceKm, double fuelPricePerL) {
        return getVehicle(vehicleId).fuelCostFor(distanceKm, fuelPricePerL);
    }

    public double estimateTripMaintenance(UUID vehicleId, double distanceKm) {
        return getVehicle(vehicleId).maintenanceCostFor(distanceKm);
    }
}

/* ===================== ДЕМО ===================== */
public class FleetDemo {
    public static void main(String[] args) {
        Fleet fleet = new Fleet();

        Vehicle c1 = new Car("Toyota Corolla", 2020, 18000, 6.5, 0.05, 45000);
        Vehicle t1 = new Truck("Volvo FH", 2018, 95000, 25.0, 0.20, 320000);
        Vehicle b1 = new Bus("Mercedes Tourismo", 2019, 150000, 18.0, 0.15, 210000);

        fleet.addVehicle(c1);
        fleet.addVehicle(t1);
        fleet.addVehicle(b1);

        System.out.println("--- Початковий стан ---");
        fleet.listAll().forEach(System.out::println);

        // Оцінки витрат на конкретну поїздку
        double dist = 350; // км
        double fuelPrice = 2.0; // $/л (приклад)
        System.out.printf("%nОцінка витрат пального для Car (%.0f км): %.2f%n",
                dist, fleet.estimateTripFuelCost(c1.getId(), dist, fuelPrice));
        System.out.printf("Оцінка ТО для Truck (%.0f км): %.2f%n",
                dist, fleet.estimateTripMaintenance(t1.getId(), dist));

        // Реєстрація поїздок
        fleet.registerTrip(c1.getId(), new Trip(LocalDate.now(), 120, "Alice", 2.05));
        fleet.registerTrip(t1.getId(), new Trip(LocalDate.now().minusDays(1), 520, "Bob", 1.95));
        fleet.registerTrip(b1.getId(), new Trip(LocalDate.now(), 300, "Carl", 1.90));

        // Ремонт (критичний)
        fleet.addRepair(t1.getId(), new Repair(LocalDate.now(), "Заміна гальмівної системи", 1800, true));
        System.out.println("\n--- Після реєстрації поїздок і ремонту ---");
        fleet.listAll().forEach(System.out::println);

        // Спроба поїздки з активним критичним ремонтом
        try {
            fleet.registerTrip(t1.getId(), new Trip(LocalDate.now(), 100, "Bob", 1.95));
        } catch (TripRegistrationException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Закрити критичні ремонти (припустимо, завершили)
        fleet.closeCriticalRepairs(t1.getId());

        // ТО і повторна поїздка
        fleet.completeService(c1.getId(), LocalDate.now());
        fleet.registerTrip(t1.getId(), new Trip(LocalDate.now(), 80, "Bob", 1.95));

        System.out.println("\n--- Фінальний стан ---");
        fleet.listAll().forEach(v -> {
            System.out.println(v);
            System.out.println("  Поїздок: " + v.getTrips().size());
            System.out.println("  Ремонтів: " + v.getRepairs().size());
        });

        // Приклади амортизації
        LocalDate asOf = LocalDate.now();
        System.out.printf("%nБалансова вартість Car на %s: %.2f%n", asOf, c1.depreciationValue(asOf));
        System.out.printf("Балансова вартість Truck на %s: %.2f%n", asOf, t1.depreciationValue(asOf));
        System.out.printf("Балансова вартість Bus на %s: %.2f%n", asOf, b1.depreciationValue(asOf));
    }
}