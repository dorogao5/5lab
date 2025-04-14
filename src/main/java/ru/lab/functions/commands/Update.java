package ru.lab.functions.commands;

import ru.lab.functions.Command;
import ru.lab.util.CollectionManager;
import ru.lab.model.Vehicle;
import ru.lab.model.Coordinates;
import ru.lab.model.VehicleType;
import ru.lab.model.FuelType;
import ru.lab.builder.Console;

/**
 * Команда для обновления значения элемента коллекции по заданному ключу.
 * Формат: update <key> - затем последовательно считываются данные нового элемента.
 */
public class Update implements Command {
    private final CollectionManager collectionManager;
    private final Console console; // Используем интерактивный ввод

    /**
     * Конструктор команды Update.
     *
     * @param collectionManager менеджер коллекции транспортных средств.
     * @param console           объект Console для считывания данных.
     */
    public Update(CollectionManager collectionManager, Console console) {
        this.collectionManager = collectionManager;
        this.console = console;
    }

    @Override
    public void execute(String[] args) {
        int updateKey;
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
                String input = console.readInteractiveLine("Ошибка: необходимо указать ключ для обновления. Введите ключ:");
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
            updateKey = Integer.parseInt(keyCandidate);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: невозможно преобразовать введённое значение в число.");
            return;
        }

        if (updateKey <= 0) {
            System.out.println("Ошибка: ключ должен быть положительным числом.");
            return;
        }
        if (!collectionManager.getCollection().containsKey(updateKey)) {
            System.out.println("Ошибка: элемент с ключом " + updateKey + " не найден.");
            return;
        }

        // Считываем данные для нового элемента через интерактивный ввод (игнорируя строки из скрипта)
        String name = promptString("Введите имя транспортного средства (не пустая строка): ", true);
        long x = promptLong("Введите координату X (0..225): ", 0, 225);
        int y = promptInt("Введите координату Y (0..493): ", 0, 493);
        float enginePower = promptFloat("Введите мощность двигателя (> 0): ", 0, Float.MAX_VALUE);
        VehicleType vtype = promptEnum("Введите тип транспортного средства (BOAT, CHOPPER, HOVERBOARD, SPACESHIP) или пустую строку для null: ", VehicleType.class);
        FuelType ftype = promptEnum("Введите тип топлива (GASOLINE, KEROSENE, NUCLEAR, PLASMA) или пустую строку для null: ", FuelType.class);

        Vehicle newVehicle = new Vehicle(0, name, new Coordinates(x, y), enginePower, vtype, ftype);
        newVehicle.setId(updateKey);
        collectionManager.getCollection().put(updateKey, newVehicle);
        System.out.println("Элемент с ключом " + updateKey + " успешно обновлен.");
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
        return "update <key> - обновить значение элемента коллекции, id которого равен заданному. \n(используйте \\stop_running_command если хотите прервать выполнение команды)";
    }
}
