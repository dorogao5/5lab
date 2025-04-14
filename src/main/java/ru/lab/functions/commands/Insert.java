package ru.lab.functions.commands;

import ru.lab.functions.Command;
import ru.lab.util.CollectionManager;
import ru.lab.model.Vehicle;
import ru.lab.model.Coordinates;
import ru.lab.model.VehicleType;
import ru.lab.model.FuelType;
import ru.lab.builder.Console;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Команда для добавления нового элемента с заданным ключом.
 * Формат: insert &lt;key> - затем последовательно считываются поля нового элемента.
 * При добавлении все элементы с ключами >= заданному сдвигаются (их ключ увеличивается на 1).
 */
public class Insert implements Command {
    private final CollectionManager collectionManager;
    private final Console console; // Используем интерактивный ввод

    /**
     * Конструктор команды Insert.
     *
     * @param collectionManager менеджер коллекции транспортных средств.
     * @param console           объект Console, предоставляющий метод readInteractiveLine.
     */
    public Insert(CollectionManager collectionManager, Console console) {
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    public void execute(String[] args) {
        int insertKey;
        String keyCandidate = null;

        if (args.length > 0) {
            for (String s : args) {
                s = s.trim();
                if (s.matches("^-?\\d+$")) {
                    keyCandidate = s;
                    break;
                }
            }
        }

        if (keyCandidate == null) {
            while (true) {
                String input = console.readInteractiveLine("Ошибка: необходимо указать ключ для вставки. Введите ключ: ");
                input = input.trim();
                if (input.matches("^-?\\d+$")) {
                    keyCandidate = input;
                    break;
                } else {
                    System.out.println("Ошибка: введено некорректное число. Ожидается целое число.");
                }
            }
        }
        try {
            insertKey = Integer.parseInt(keyCandidate);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: невозможно преобразовать введённое значение в число.");
            return;
        }

        Hashtable<Integer, Vehicle> collection = collectionManager.getCollection();
        if (insertKey <= 0) {
            System.out.println("Ошибка: ключ должен быть положительным числом.");
            return;
        }
        int maxKey = collection.isEmpty() ? 0 : collection.size();
        if (insertKey > maxKey + 1) {System.out.println("Ошибка: Невозможно вставить элемент с ключом " + insertKey +
                        ". Максимальный допустимый ключ: " + (maxKey + 1));
            return;
        }

        // Считываем данные для нового элемента через интерактивный ввод (игнорируя строки из скрипта)
        String name = promptString("Введите имя транспортного средства (не пустая строка): ", true);
        long x = promptLong("Введите координату X (0..225): ", 0, 225);
        int y = promptInt("Введите координату Y (0..493): ", 0, 493);
        float enginePower = promptFloat("Введите мощность двигателя (> 0): ", 0, Float.MAX_VALUE);
        VehicleType vtype = promptEnum("Введите тип транспортного средства (BOAT, CHOPPER, HOVERBOARD, SPACESHIP) или пустую строку для null: ", VehicleType.class);
        FuelType ftype = promptEnum("Введите тип топлива (GASOLINE, KEROSENE, NUCLEAR, PLASMA) или пустую строку для null: ", FuelType.class);

        // Создаем новый объект Vehicle
        Vehicle newVehicle = new Vehicle(0, name, new Coordinates(x, y), enginePower, vtype, ftype);
        newVehicle.setId(insertKey);

        // Сдвигаем элементы с ключами >= insertKey
        List<Integer> keysToShift = new ArrayList<>();
        for (Integer key : collection.keySet()) {
            if (key >= insertKey) {
                keysToShift.add(key);
            }
        }
        Collections.sort(keysToShift, Collections.reverseOrder());
        for (Integer key : keysToShift) {
            Vehicle v = collection.remove(key);
            int newKey = key + 1;
            v.setId(newKey);
            collection.put(newKey, v);
        }
        // Вставляем новый элемент
        collection.put(insertKey, newVehicle);
        System.out.println("Элемент успешно добавлен с ключом " + insertKey + ".");
    }

    private String promptString(String prompt, boolean nonEmpty) {
        String input;
        while (true) {
            input = console.readInteractiveLine(prompt);
            if (nonEmpty && (input == null || input.trim().isEmpty())) {
                System.out.println("Ошибка: строка не может быть пустой.");
            } else {
                break;
            }
        }
        return input;
    }

    private long promptLong(String prompt, long min, long max) {
        while (true) {
            String input = console.readInteractiveLine(prompt);
            try {
                long value = Long.parseLong(input);
                if (value < min || value > max) {
                    System.out.println("Ошибка: значение должно быть в диапазоне [" + min + ", " + max + "].");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число.");
            }
        }
    }

    private int promptInt(String prompt, int min, int max) {
        while (true) {
            String input = console.readInteractiveLine(prompt);
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Ошибка: значение должно быть в диапазоне [" + min + ", " + max + "].");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private float promptFloat(String prompt, float min, float max) {
        while (true) {
            String input = console.readInteractiveLine(prompt);
            try {
                float value = Float.parseFloat(input);
                if (value <= min || value > max) {
                    System.out.println("Ошибка: значение должно быть больше " + min + " и не превышать " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число с плавающей запятой.");
            }
        }
    }

    private <E extends Enum<E>> E promptEnum(String prompt, Class<E> enumClass) {
        while (true) {
            String input = console.readInteractiveLine(prompt);
            if (input == null || input.trim().isEmpty()) {
                return null;
            }
            try {
                return Enum.valueOf(enumClass, input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.print("Доступные значения: ");
                for (E constant : enumClass.getEnumConstants()) {
                    System.out.print(constant.name() + " ");
                }
                System.out.println();
                System.out.println("Ошибка: введите одно из указанных значений.");
            }
        }
    }

    @Override
    public String getDescription() {
        return "insert <key> - добавить новый элемент с заданным ключом, сдвигая элементы с ключами >= заданного. \n(используйте \\stop_running_command если хотите прервать выполнение команды)";
    }
}
