package org.example;

import java.util.Scanner;
import java.util.LinkedList; //библиотека ссылочного списка, в котором элементы(узлы) связаны ссылками на следующий и предыдущий элемент(так и создается упорядоченность), что упрощает вставку новых элементов
import java.util.Queue; //интерфейс очереди
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

//создаем класс для описания машины Car
class Car {
    double tank_capacity; //объем бака
    double current_fuel; //сколько топлива в баке
    double wanted_fuel; //желаемое количество топлива
    int arrive_time; //время прибытия

    //конструктор класса, которому задаются объем бака, текущий уровень топлива, желаемый объем и время прибытия
    public Car(double tank_capacity, double current_fuel, double wanted_fuel, int arrive_time) {
        this.tank_capacity = tank_capacity; //this указывает на переменную класса с таким же названием
        this.current_fuel = current_fuel;
        this.wanted_fuel = wanted_fuel;
        this.arrive_time = arrive_time;
    }

    //метод для посчета свободного места дл топлива
    public double free_space() {
        return tank_capacity - current_fuel;
    }
    //метод для проверки вместимости желаемого объема топлива
    public double refuel(double amount) {
        if (current_fuel + amount <= tank_capacity) {
            current_fuel += amount; //если помещается, то заливаем топливо
        }else{
            amount = tank_capacity - current_fuel;
            current_fuel = tank_capacity; //если не помещается, тогда вливаем столько, сколько вместимся, и return кол-во литров, которые залили
        }
        return amount;
    }
}

//создаем класс запровочной колонки Pump
class Pump {
    double fuel_speed; //скорость заправки (л/мин)
    boolean is_occupied; //флажок для занятых колонок
    Car current_car; //текущая машина на заправке
    int time_occupied; //время заправки (занятости)
    Scanner scanner;
    //конструктор, которму подается скорость заправки
    public Pump(double fuel_speed) {
        this.fuel_speed = fuel_speed;
        this.is_occupied = false; //флажок по умолчанию опущен
        this.time_occupied = 0;//начальное время 0 мин
        this.scanner = scanner;
    }

    //метод для обозначения начала заправки, подает на ввод объект Car
    public void start_refuel(Car car) {
        is_occupied = true;
        current_car = car;
        time_occupied = 0;
        System.out.println("Машина начала заправку.");
    }
    //метод для обозначения конца заправки
    public void stop_refuel() {
        is_occupied = false;
        current_car = null;
        System.out.println("Машина закончила заправку.");
    }
}

//создаем класс заправочной станции Gas_station
class Gas_station {
    Queue<Car> queue; // объявляем переменную, которая будет ссылаться на объект, реализующий интрефейс очереди
    List<Pump> pumps; //список колонок
    double fuel_reserve; //запас топлива на станции
    private Scanner scanner; //инициализируем локальный сканер
    int next_pump_open_time; //таймер для дополнительных колонок которые будут открываться при сильной загрузке
    int time_for_fuel;

    //конструктор, принимаюищий кол-во колонок и начальный запаса топлива
    public Gas_station(int num_pumps, double init_fuel, Scanner scanner) {
        queue = new LinkedList<>(); //создаем ссылочный список с интрефейсом очереди
        pumps = new ArrayList<>();
        time_for_fuel = 1440; //каждые 1440 минут (сутки) запас топлива на станции будет пополняться
        next_pump_open_time = -1; //пока что доп колонки не нужны, таймер стоит на -1
        this.scanner = scanner; //инициализируем локальный сканнер, чтобы можно было его использовать в методах класса

        for (int i = 0; i < num_pumps; i++) { //запускаем цикл, который назначает каждой колонке скорость заправки
            System.out.print("Введите скорость заправки колонки №" + (i + 1) + " : ");
            double fuel_speed = scanner.nextDouble(); //спрашиваем у пользователя скорость заправки
            pumps.add(new Pump(fuel_speed));
        }
        this.fuel_reserve = init_fuel;
    }

    //метод для добавления машины в очередь
    public void add_to_queue(Car car) {
        queue.add(car);
        System.out.println("Машина добавлена в очередь.");
    }
    //запускаем очередь, получая текущеее время
    public void running_queue(int current_time) {
        for (Pump check_pump : pumps) { //переменная check_pump типа Pump проходится по списку pumps как i
            if (!check_pump.is_occupied && !queue.isEmpty()) { //если колонка пустая и очередь не пустая
                Car car_for_refuel = queue.poll(); //то метод poll() возвращает первую машину в очереди(мы ее закидываем в переменую car_for_refuel) и удаляет из очереди
                check_pump.start_refuel(car_for_refuel);
            }
        }

        //проверка времени ожидания, и если оно больше 12 мин, то открывается новую колонку
        if (next_pump_open_time == -1) {
            for (Car car : queue) { //проходимся по очереди из машин
                if (current_time - car.arrive_time > 12) { //проверяем разность текущего времени и времени прибытия
                    next_pump_open_time = current_time + 2880; // заводим таймер на 2 дня
                    break;
                }
            }
        }
        // добавляем колонку, если время таймера закончилось
        if (current_time == next_pump_open_time) {
            System.out.print("По многочисленным просьбам открылась новая колонка! Введите ее скорость заправки: ");
            double fuel_speed = scanner.nextDouble(); //запрашиваем скорость у пользователя
            pumps.add(new Pump(fuel_speed));
            next_pump_open_time = -1; //обновлем таймер
        }


        //пополнение запаса топлива на станции
        if (current_time % time_for_fuel == 0) {
            System.out.println("Пришла поставка топлива на станцию.");
            fuel_reserve += 1000; //если заданное время прошло, то пополняем запасы топлива на станции
        }

    }

}

//создаем класс симуляции Simulation
class Simulation {
    Gas_station gas_station; //создаем экземпляр станции
    int time; //реальное время
    Random random;
    private Scanner scanner;

    //конструктор
    public Simulation(int num_pumps, double init_fuel, Scanner scanner) {
        gas_station = new Gas_station(num_pumps, init_fuel, scanner);
        time = 0;
        random = new Random();
    }

    //запуск с отсчетом времени
    public void run(int duration) { //принимает длительность симуляции
        for (int t = 0; t < duration; t++) {
            time = t; //текущее время
            System.out.println("Время: " + time);
            generate_car();
            gas_station.running_queue(time);
            update();
        }
    }

    //генерируем машину
    public void generate_car() {
        if (random.nextDouble() < 0.1) { // шанс генерации машины 10%
            System.out.println("Приехала новая машина.");
            double tank_capacity = 50 + random.nextDouble() * 10; //случайный объем бака
            double current_fuel = tank_capacity * random.nextDouble() ; //случайный текущий объем топлива
            double wanted_fuel = tank_capacity * random.nextDouble() * 0.5; //случайный желаемый объем топлива
            Car car = new Car(tank_capacity, current_fuel, wanted_fuel, time);
            gas_station.add_to_queue(car); //запускаем машину в очередь
        }
    }

    //метод для обновления всех данных
    public void update() {
        for (Pump pump : gas_station.pumps) {
            if (pump.is_occupied) {
                Car car = pump.current_car; //кладем в переменную car машину, которая находится в данный момент у колонки
                double amount = Math.min(pump.fuel_speed, car.wanted_fuel); // если желаемый объем больше скорости заправки, разделяем его на части
                if (gas_station.fuel_reserve > 0) {
                    amount = car.refuel(amount); //сколько поместилось в бак
                    car.wanted_fuel -= amount; //вычитаем заправленную часть из желаемой
                    gas_station.fuel_reserve -= amount; //вычитаем заправленную часть из общего запаса топлива на заправке
                    System.out.println("Текущее количество топлива на станции: " + gas_station.fuel_reserve);
                }else{
                    continue;
                }
                pump.time_occupied++;
                if (amount == 0 || car.wanted_fuel == 0) { //если ничего не заправили (не поместилось) или если все заправили сколько хотели, закнчиваем заправку
                    pump.stop_refuel();
                }
            }
        }
    }
}

//запускаем все
public class GAS {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in); //открываем сканнер
        System.out.println("Введите количество колонок: ");
        int num_pumps = scanner.nextInt(); //запрашиваем кол - во колонок
        System.out.println("Введите начальное количество топлива на станции: ");
        Double init_fuel = scanner.nextDouble(); //запрашиваем запас топлива на станции
        System.out.println("Введите время работы: ");
        int duration = scanner.nextInt(); //и время работы симуляции в минутах

        // Создаем объект Simulation по запросам пользователя
        Simulation simulation = new Simulation(num_pumps, init_fuel, scanner);
        // Запускаем симуляцию
        simulation.run(duration);

        scanner.close();
    }
}
