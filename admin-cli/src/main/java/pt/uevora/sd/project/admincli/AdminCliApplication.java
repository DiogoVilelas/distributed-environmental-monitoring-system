package pt.uevora.sd.project.admincli;

import pt.uevora.sd.project.admincli.dto.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class AdminCliApplication {

    public static void main(String[] args) throws Exception {
        String baseUrl = (args.length >= 1) ? args[0] : "http://localhost:8080";
        ApiClient api = new ApiClient(baseUrl);
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== ADMIN CLI ===");
            System.out.println("1) Gestão de Dispositivos");
            System.out.println("2) Consulta de Métricas");
            System.out.println("3) Estatísticas do Sistema");
            System.out.println("0) Sair");
            System.out.print("> ");
            String op = in.nextLine().trim();

            switch (op) {
                case "1" -> devicesMenu(api, in);
                case "2" -> metricsMenu(api, in);
                case "3" -> statsMenu(api);
                case "0" -> { System.out.println("Bye."); return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void devicesMenu(ApiClient api, Scanner in) throws Exception {
        while (true) {
            System.out.println("\n--- Gestão de Dispositivos ---");
            System.out.println("1) Listar todos");
            System.out.println("2) Adicionar");
            System.out.println("3) Atualizar");
            System.out.println("4) Remover");
            System.out.println("5) Ver detalhes");
            System.out.println("0) Voltar");
            System.out.print("> ");
            String op = in.nextLine().trim();

            switch (op) {
                case "1" -> printDevices(api.listDevices());

                case "2" -> {
                    DeviceDto d = readDevice(in, null);
                    System.out.println(api.createDevice(d));
                }

                case "3" -> {
                    System.out.print("ID a atualizar: ");
                    String typed = in.nextLine().trim();

                    String id = resolveDeviceIdIgnoreCase(api, typed);
                    if (id == null) {
                        System.out.println("Dispositivo não existe: " + typed);
                        break;
                    }

                    DeviceDto current = api.getDevice(id);
                    DeviceDto updated = readDevice(in, current);
                    System.out.println(api.updateDevice(id, updated));
                }

                case "4" -> {
                    System.out.print("ID a remover: ");
                    String typed = in.nextLine().trim();

                    String id = resolveDeviceIdIgnoreCase(api, typed);
                    if (id == null) {
                        System.out.println("Dispositivo não existe: " + typed);
                        break;
                    }

                    System.out.println(api.deleteDevice(id));
                }

                case "5" -> {
                    System.out.print("ID: ");
                    String typed = in.nextLine().trim();

                    String id = resolveDeviceIdIgnoreCase(api, typed);
                    if (id == null) {
                        System.out.println("Dispositivo não existe: " + typed);
                        break;
                    }

                    DeviceDto d = api.getDevice(id);
                    System.out.println("\nID: " + d.id);
                    System.out.println("Protocol: " + d.protocol);
                    System.out.println("Building: " + d.building);
                    System.out.println("Floor: " + d.floor);
                    System.out.println("Department: " + d.department);
                    System.out.println("Room: " + d.room);
                    System.out.println("Status: " + d.status);
                }

                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static DeviceDto readDevice(Scanner in, DeviceDto base) {
        DeviceDto d = new DeviceDto();
        d.id = ask(in, "id", base != null ? base.id : "");
        d.protocol = ask(in, "protocol (MQTT/GRPC/REST)", base != null ? base.protocol : "REST");
        d.building = ask(in, "building (ex: CLAV, MITRA)", base != null ? base.building : "CLAV");
        d.floor = Integer.parseInt(ask(in, "floor", base != null ? String.valueOf(base.floor) : "0"));
        d.department = ask(in, "department", base != null ? base.department : "geral");
        d.room = ask(in, "room (ex: 136, anf1, biblioteca)", base != null ? base.room : "anf1");
        d.status = ask(in, "status (ACTIVE/INACTIVE)", base != null ? base.status : "ACTIVE");
        return d;
    }

    private static String ask(Scanner in, String label, String def) {
        System.out.print(label + (def != null && !def.isBlank() ? " [" + def + "]" : "") + ": ");
        String s = in.nextLine().trim();
        return s.isBlank() ? def : s;
    }

    private static void printDevices(List<DeviceDto> list) {
        System.out.println("\nID | PROTOCOL | BUILDING | FLOOR | DEPT | ROOM | STATUS");
        System.out.println("-------------------------------------------------------");
        for (DeviceDto d : list) System.out.println(d);
    }

    private static void metricsMenu(ApiClient api, Scanner in) throws Exception {
        while (true) {
            System.out.println("\n--- Consulta de Métricas ---");
            System.out.println("1) Consultar por sala");
            System.out.println("2) Consultar por departamento");
            System.out.println("3) Consultar por piso");
            System.out.println("4) Consultar por edifício");
            System.out.println("5) Especificar intervalo de datas");
            System.out.println("6) Visualizar dados em formato tabular"); // extra, mas útil
            System.out.println("0) Voltar");
            System.out.print("> ");
            String op = in.nextLine().trim();

            switch (op) {
                case "1" -> consultByRoom(api, in);
                case "2" -> consultByDepartment(api, in);
                case "3" -> consultByFloor(api, in);
                case "4" -> consultByBuilding(api, in);
                case "5" -> consultByDateRangeAll(api, in);
                case "6" -> consultAllTabular(api);
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void consultAllTabular(ApiClient api) throws Exception {
        System.out.println("\n--- Todas as métricas (formato tabular) ---");
        List<MetricDto> all = api.allMetrics(null, null);

        all.sort(Comparator.comparing(m -> m.timestamp));
        printMetricsTable(all);
        System.out.println("(total: " + all.size() + ")");
    }

    private static void consultByRoom(ApiClient api, Scanner in) throws Exception {
        System.out.println("\nFiltro -> Sala (room)  ex: 136, anf1, biblioteca");
        System.out.print("> ");
        String room = in.nextLine().trim();
        if (room.isBlank()) { System.out.println("Sala vazia."); return; }

        Instant from = readInstantOptional(in, "from");
        Instant to   = readInstantOptional(in, "to");

        List<DeviceDto> devices = api.listDevices().stream()
                .filter(d -> d.room != null && d.room.equalsIgnoreCase(room))
                .collect(Collectors.toList());

        runMetricQueryFlow(api, in, devices, from, to, "ROOM", room);
    }

    private static void consultByDepartment(ApiClient api, Scanner in) throws Exception {
        System.out.println("\nFiltro -> Departamento  ex: informatica, veterinaria, geral");
        System.out.print("> ");
        String dept = in.nextLine().trim();
        if (dept.isBlank()) { System.out.println("Departamento vazio."); return; }

        Instant from = readInstantOptional(in, "from");
        Instant to   = readInstantOptional(in, "to");

        List<DeviceDto> devices = api.listDevices().stream()
                .filter(d -> d.department != null && d.department.equalsIgnoreCase(dept))
                .collect(Collectors.toList());

        runMetricQueryFlow(api, in, devices, from, to, "DEPARTMENT", dept);
    }

    private static void consultByBuilding(ApiClient api, Scanner in) throws Exception {
        System.out.println("\nFiltro -> Edifício  ex: CLAV, MITRA");
        System.out.print("> ");
        String building = in.nextLine().trim();
        if (building.isBlank()) { System.out.println("Edifício vazio."); return; }

        Instant from = readInstantOptional(in, "from");
        Instant to   = readInstantOptional(in, "to");

        List<DeviceDto> devices = api.listDevices().stream()
                .filter(d -> d.building != null && d.building.equalsIgnoreCase(building))
                .collect(Collectors.toList());

        runMetricQueryFlow(api, in, devices, from, to, "BUILDING", building);
    }

    private static void consultByFloor(ApiClient api, Scanner in) throws Exception {
        System.out.println("\nFiltro -> Piso (building + floor)");
        System.out.print("Building (ex: CLAV): ");
        String building = in.nextLine().trim();
        System.out.print("Floor (ex: 0, 1): ");
        String floorStr = in.nextLine().trim();

        if (building.isBlank() || floorStr.isBlank()) {
            System.out.println("Building/floor inválidos.");
            return;
        }

        int floor;
        try {
            floor = Integer.parseInt(floorStr);
        } catch (Exception e) {
            System.out.println("Floor tem de ser número inteiro.");
            return;
        }

        Instant from = readInstantOptional(in, "from");
        Instant to   = readInstantOptional(in, "to");

        List<DeviceDto> devices = api.listDevices().stream()
                .filter(d -> d.building != null && d.building.equalsIgnoreCase(building) && d.floor == floor)
                .collect(Collectors.toList());

        runMetricQueryFlow(api, in, devices, from, to, "FLOOR", building + ":" + floor);
    }

    private static void consultByDateRangeAll(ApiClient api, Scanner in) throws Exception {
        System.out.println("\n--- Intervalo de datas (todas as métricas) ---");
        Instant from = readInstantRequired(in, "from");
        Instant to   = readInstantRequired(in, "to");

        List<MetricDto> all = api.allMetrics(from, to);

        all.sort(Comparator.comparing(m -> m.timestamp));
        printMetricsTable(all);
        System.out.println("(total: " + all.size() + ")");
    }

    private static void runMetricQueryFlow(
            ApiClient api,
            Scanner in,
            List<DeviceDto> devicesInScope,
            Instant from,
            Instant to,
            String scopeLabel,
            String scopeKey
    ) throws Exception {

        if (devicesInScope.isEmpty()) {
            System.out.println("Não há dispositivos para esse filtro (" + scopeLabel + "=" + scopeKey + ").");
            return;
        }

        while (true) {
            System.out.println("\nO que queres consultar?");
            System.out.println("1) Consultar valor médio");
            System.out.println("2) Consultar valor mais recente");
            System.out.println("0) Voltar");
            System.out.print("> ");
            String op = in.nextLine().trim();

            switch (op) {
                case "1" -> {
                    AverageDto avg = api.averageSingle(scopeLabel, scopeKey, from, to);

                    if (avg == null || avg.count == 0) {
                        System.out.println("Sem métricas nesse intervalo.");
                    } else {
                        System.out.println("\nLEVEL | KEY | AVG_TEMP | AVG_HUM | COUNT");
                        System.out.println("----------------------------------------");
                        System.out.printf("%s | %s | %.2f | %.2f | %d%n",
                                avg.level, avg.key, avg.avgTemperature, avg.avgHumidity, avg.count);
                    }
                }
                case "2" -> {
                    MetricDto last = computeLatestOverDevices(api, devicesInScope, from, to);
                    if (last == null) {
                        System.out.println("Sem métricas nesse intervalo.");
                    } else {
                        System.out.println("\nID | DEVICE | TEMP | HUM | TIMESTAMP");
                        System.out.println("------------------------------------");
                        System.out.printf("%d | %s | %.2f | %.2f | %s%n",
                                last.id, last.deviceId, last.temperature, last.humidity, last.timestamp);
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static MetricDto computeLatestOverDevices(ApiClient api, List<DeviceDto> devices,
                                                     Instant from, Instant to) throws Exception {

        MetricDto best = null;

        for (DeviceDto d : devices) {
            List<MetricDto> rows = api.rawMetrics(d.id, from, to);
            for (MetricDto m : rows) {
                if (best == null) best = m;
                else if (m.timestamp != null && best.timestamp != null && m.timestamp.isAfter(best.timestamp)) best = m;
                else if (best.timestamp == null && m.timestamp != null) best = m;
            }
        }
        return best;
    }

    private static void printMetricsTable(List<MetricDto> rows) {
        System.out.println("\nID | DEVICE | TEMP | HUM | TIMESTAMP");
        System.out.println("------------------------------------");
        for (MetricDto m : rows) {
            System.out.printf("%d | %s | %.2f | %.2f | %s%n",
                    m.id, m.deviceId, m.temperature, m.humidity, m.timestamp);
        }
    }

    private static void statsMenu(ApiClient api) throws Exception {
        List<DeviceDto> devices = api.listDevices();

        long total = devices.size();
        long active = devices.stream().filter(d -> "ACTIVE".equalsIgnoreCase(d.status)).count();
        Map<String, Long> byProto = devices.stream()
                .collect(Collectors.groupingBy(d -> d.protocol.toUpperCase(), Collectors.counting()));

        System.out.println("\n--- Estatísticas do Sistema ---");
        System.out.println("Total devices: " + total);
        System.out.println("Active devices: " + active);
        System.out.println("By protocol: " + byProto);
    }

    private static String resolveDeviceIdIgnoreCase(ApiClient api, String typed) throws Exception {
        if (typed == null || typed.isBlank()) return null;
        for (DeviceDto d : api.listDevices()) {
            if (d.id != null && d.id.equalsIgnoreCase(typed)) return d.id;
        }
        return null;
    }

    private static Instant readInstantOptional(Scanner in, String label) {
        System.out.println(label + " ISO-8601 (enter vazio = sem filtro)");
        System.out.println("  exemplos:");
        System.out.println("   - 2025-12-24T12:30:00Z");
        System.out.println("   - 2025-12-24T12:30:00.000Z");
        System.out.print(label + ": ");
        String s = in.nextLine().trim();
        if (s.isBlank()) return null;

        try {
            return Instant.parse(s);
        } catch (Exception e) {
            System.out.println("Formato inválido. A ignorar filtro " + label + ".");
            return null;
        }
    }

    private static Instant readInstantRequired(Scanner in, String label) {
        while (true) {
            System.out.println(label + " ISO-8601 (obrigatório)");
            System.out.println("  exemplos:");
            System.out.println("   - 2025-12-24T12:30:00Z");
            System.out.println("   - 2025-12-24T12:30:00.000Z");
            System.out.print(label + ": ");
            String s = in.nextLine().trim();

            try {
                return Instant.parse(s);
            } catch (Exception e) {
                System.out.println("Formato inválido. Tenta novamente.");
            }
        }
    }
}
