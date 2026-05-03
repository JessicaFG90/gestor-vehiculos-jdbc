package view;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import model.domain.Vehiculo;
import model.service.VehiculoService;

/**
 * Interfaz de consola para gestionar vehículos.
 *
 * Esta clase es la "vista" en el patrón MVC: solo se encarga de mostrar
 * información al usuario y recoger sus datos. Nunca accede directamente
 * a la base de datos; para eso usa VehiculoService (la capa de servicio).
 *
 * Sigue el patrón Singleton: solo puede existir una instancia de esta clase.
 */
public class VehiculoConsoleUI {

    // ── Códigos ANSI ──────────────────────────────────────────────────────────
    // Los códigos ANSI permiten añadir color y formato al texto de la consola.
    // Funcionan en terminales de Linux/macOS y en la mayoría de IDEs modernos.
    private static final String RESET   = "\u001B[0m";  // Cancela todos los efectos activos
    private static final String BOLD    = "\u001B[1m";  // Texto en negrita
    private static final String CYAN    = "\u001B[36m"; // Color cian (títulos y opciones)
    private static final String GREEN   = "\u001B[32m"; // Verde (mensajes de éxito)
    private static final String YELLOW  = "\u001B[33m"; // Amarillo (advertencias)
    private static final String RED     = "\u001B[31m"; // Rojo (errores)
    private static final String MAGENTA = "\u001B[35m"; // Magenta (prompts de entrada)
    private static final String WHITE   = "\u001B[37m"; // Blanco (texto secundario)
    private static final String BG_CYAN = "\u001B[46m"; // Fondo cian (cabecera del menú)

    // ── Singleton ─────────────────────────────────────────────────────────────
    // La instancia única se guarda aquí. Al ser static, pertenece a la clase,
    // no a ningún objeto concreto.
    private static VehiculoConsoleUI instance = null;

    private final VehiculoService vehiculoService; // Lógica de negocio y validaciones
    private final Scanner scanner;                 // Lee lo que escribe el usuario

    /** Constructor privado: impide crear instancias desde fuera de la clase. */
    private VehiculoConsoleUI(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Devuelve la única instancia de VehiculoConsoleUI.
     * Si todavía no existe, la crea (lazy initialization).
     *
     * @param vehiculoService Capa de servicio que gestiona los vehículos.
     */
    public static VehiculoConsoleUI getInstance(VehiculoService vehiculoService) {
        if (instance == null) {
            instance = new VehiculoConsoleUI(vehiculoService);
        }
        return instance;
    }

    // ── Método principal ──────────────────────────────────────────────────────

    /**
     * Arranca el bucle principal de la aplicación.
     * Muestra el menú repetidamente hasta que el usuario elige salir.
     */
    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Elige una opción: ");
            System.out.println();

            // Cada case llama al método que gestiona esa operación
            switch (opcion) {
                case 1 -> listarTodos();
                case 2 -> buscarPorId();
                case 3 -> buscarPorMarca();
                case 4 -> crearVehiculo();
                case 5 -> actualizarVehiculo();
                case 6 -> eliminarVehiculo();
                case 0 -> salir = confirmarSalida();
                default -> printAdvertencia("Opción no válida. Inténtalo de nuevo.");
            }

            // Pausa al final de cada operación (excepto al salir)
            if (!salir) pausar();
        }

        printInfo("¡Hasta pronto!");
        scanner.close(); // Libera el recurso del Scanner al terminar
    }

    // ── Menú ──────────────────────────────────────────────────────────────────

    /** Imprime el menú principal con todas las opciones disponibles. */
    private void mostrarMenu() {
        System.out.println();
        System.out.println(BG_CYAN + BOLD + WHITE + "  ══════════════════════════════════  " + RESET);
        System.out.println(BG_CYAN + BOLD + WHITE + "       GESTIÓN DE VEHÍCULOS           " + RESET);
        System.out.println(BG_CYAN + BOLD + WHITE + "  ══════════════════════════════════  " + RESET);
        System.out.println(CYAN + "  1." + RESET + " Listar todos los vehículos");
        System.out.println(CYAN + "  2." + RESET + " Buscar vehículo por ID");
        System.out.println(CYAN + "  3." + RESET + " Buscar vehículos por marca");
        System.out.println(CYAN + "  4." + RESET + " Dar de alta un vehículo");
        System.out.println(CYAN + "  5." + RESET + " Actualizar un vehículo");
        System.out.println(CYAN + "  6." + RESET + " Eliminar un vehículo");
        System.out.println(RED   + "  0." + RESET + " Salir");
        System.out.println(CYAN  + "  ────────────────────────────────────" + RESET);
    }

    // ── Operaciones CRUD ──────────────────────────────────────────────────────

    /** Recupera todos los vehículos de la base de datos y los muestra por pantalla. */
    private void listarTodos() {
        printTitulo("LISTADO DE VEHÍCULOS");
        ArrayList<Vehiculo> vehiculos = vehiculoService.findAll();

        if (vehiculos.isEmpty()) {
            printAdvertencia("No hay vehículos registrados.");
        } else {
            // forEach recorre la lista y llama a printVehiculo por cada elemento
            vehiculos.forEach(this::printVehiculo);
            printInfo("Total: " + vehiculos.size() + " vehículo(s).");
        }
    }

    /** Pide un ID al usuario y muestra el vehículo correspondiente si existe. */
    private void buscarPorId() {
        printTitulo("BUSCAR POR ID");
        int id = leerEntero("ID del vehículo: ");

        // findById devuelve un Optional: puede contener un vehículo o estar vacío
        Optional<Vehiculo> resultado = vehiculoService.findById(id);

        if (resultado.isPresent()) {
            printVehiculo(resultado.get()); // get() extrae el vehículo del Optional
        } else {
            printAdvertencia("No se encontró ningún vehículo con ID " + id + ".");
        }
    }

    /** Pide una marca (o fragmento) y muestra todos los vehículos que coincidan. */
    private void buscarPorMarca() {
        printTitulo("BUSCAR POR MARCA");
        String marca = leerTexto("Marca (o fragmento): ");
        ArrayList<Vehiculo> vehiculos = vehiculoService.findByMarca(marca);

        if (vehiculos.isEmpty()) {
            printAdvertencia("No se encontraron vehículos con la marca \"" + marca + "\".");
        } else {
            vehiculos.forEach(this::printVehiculo);
            printInfo("Total: " + vehiculos.size() + " vehículo(s) encontrado(s).");
        }
    }

    /** Pide los datos de un nuevo vehículo al usuario y lo guarda en la base de datos. */
    private void crearVehiculo() {
        printTitulo("ALTA DE VEHÍCULO");

        // Creamos un Vehiculo vacío y lo rellenamos con los datos que introduce el usuario
        Vehiculo vehiculo = pedirDatosVehiculo(new Vehiculo());

        try {
            vehiculoService.create(vehiculo);
            printExito("Vehículo creado correctamente.");
        } catch (IllegalArgumentException e) {
            // VehiculoService lanza esta excepción si algún campo no es válido
            printError("Error de validación: " + e.getMessage());
        }
    }

    /** Pide un ID, carga ese vehículo, permite editarlo y guarda los cambios. */
    private void actualizarVehiculo() {
        printTitulo("ACTUALIZAR VEHÍCULO");
        int id = leerEntero("ID del vehículo a actualizar: ");

        Optional<Vehiculo> existente = vehiculoService.findById(id);

        if (existente.isEmpty()) {
            printAdvertencia("No se encontró ningún vehículo con ID " + id + ".");
            return; // Salimos del método sin hacer nada más
        }

        // Mostramos los datos actuales antes de pedir los nuevos
        printInfo("Datos actuales:");
        printVehiculo(existente.get());
        System.out.println();

        // Rellenamos el mismo objeto con los nuevos datos (preserva el ID)
        Vehiculo vehiculo = pedirDatosVehiculo(existente.get());
        vehiculo.setId(id); // Nos aseguramos de que el ID no cambia

        try {
            vehiculoService.update(vehiculo);
            printExito("Vehículo actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            printError("Error de validación: " + e.getMessage());
        }
    }

    /** Pide un ID, muestra el vehículo y lo elimina si el usuario confirma. */
    private void eliminarVehiculo() {
        printTitulo("ELIMINAR VEHÍCULO");
        int id = leerEntero("ID del vehículo a eliminar: ");

        Optional<Vehiculo> existente = vehiculoService.findById(id);

        if (existente.isEmpty()) {
            printAdvertencia("No se encontró ningún vehículo con ID " + id + ".");
            return;
        }

        // Mostramos el vehículo que se va a eliminar para que el usuario lo confirme
        printVehiculo(existente.get());
        System.out.print(YELLOW + "¿Confirmas la eliminación? (s/n): " + RESET);
        String confirmacion = scanner.nextLine().trim();

        if (confirmacion.equalsIgnoreCase("s")) {
            vehiculoService.deleteById(id);
            printExito("Vehículo eliminado correctamente.");
        } else {
            printInfo("Operación cancelada.");
        }
    }

    /** Pregunta al usuario si realmente quiere salir. Devuelve true si dice que sí. */
    private boolean confirmarSalida() {
        System.out.print(YELLOW + "¿Seguro que quieres salir? (s/n): " + RESET);
        String respuesta = scanner.nextLine().trim();
        return respuesta.equalsIgnoreCase("s");
    }

    // ── Helpers de entrada ────────────────────────────────────────────────────

    /**
     * Pide al usuario que rellene los campos de un vehículo.
     * Si el vehículo ya tiene datos (caso "actualizar"), los muestra como valor por defecto.
     *
     * @param vehiculo Objeto donde se guardarán los datos introducidos.
     * @return El mismo objeto vehiculo con los nuevos valores asignados.
     */
    private Vehiculo pedirDatosVehiculo(Vehiculo vehiculo) {
        // Si el campo ya tiene un valor, lo mostramos entre corchetes como sugerencia
        String marcaActual    = vehiculo.getMarca()    != null ? " [" + vehiculo.getMarca()    + "]" : "";
        String modeloActual   = vehiculo.getModelo()   != null ? " [" + vehiculo.getModelo()   + "]" : "";
        String matriculaActual = vehiculo.getMatricula() != null ? " [" + vehiculo.getMatricula() + "]" : "";

        String marca     = leerTexto("Marca"     + marcaActual    + ": ");
        String modelo    = leerTexto("Modelo"    + modeloActual   + ": ");
        String matricula = leerTexto("Matrícula" + matriculaActual + ": ");
        double consumo   = leerDouble("Consumo homologado (L/100km): ");

        // Solo actualizamos el campo si el usuario escribió algo;
        // si pulsó ENTER sin escribir nada, conservamos el valor anterior
        if (!marca.isBlank())     vehiculo.setMarca(marca);
        if (!modelo.isBlank())    vehiculo.setModelo(modelo);
        if (!matricula.isBlank()) vehiculo.setMatricula(matricula);
        vehiculo.setConsumoHomologado(consumo);

        return vehiculo;
    }

    /**
     * Muestra un prompt y devuelve el texto introducido por el usuario.
     *
     * @param prompt Texto que se muestra antes del cursor de entrada.
     */
    private String leerTexto(String prompt) {
        System.out.print(MAGENTA + prompt + RESET);
        return scanner.nextLine().trim();
    }

    /**
     * Muestra un prompt y devuelve un número entero.
     * Si el usuario escribe algo que no es un número, pide que lo repita.
     *
     * @param prompt Texto que se muestra antes del cursor de entrada.
     */
    private int leerEntero(String prompt) {
        while (true) {
            System.out.print(MAGENTA + prompt + RESET);
            String linea = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                // parseInt lanza esta excepción si la cadena no es un número entero válido
                printError("Introduce un número entero válido.");
            }
        }
    }

    /**
     * Muestra un prompt y devuelve un número decimal.
     * Acepta tanto coma como punto como separador decimal ("6,4" y "6.4" son válidos).
     *
     * @param prompt Texto que se muestra antes del cursor de entrada.
     */
    private double leerDouble(String prompt) {
        while (true) {
            System.out.print(MAGENTA + prompt + RESET);
            // replace(",", ".") permite escribir "6,4" en lugar de "6.4"
            String linea = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(linea);
            } catch (NumberFormatException e) {
                printError("Introduce un número decimal válido (ej: 6.4).");
            }
        }
    }

    /** Espera a que el usuario pulse ENTER antes de volver al menú. */
    private void pausar() {
        System.out.print(WHITE + "\nPulsa ENTER para continuar..." + RESET);
        scanner.nextLine();
    }

    // ── Helpers de salida ─────────────────────────────────────────────────────

    /**
     * Muestra un vehículo formateado en una sola línea con colores.
     * Ejemplo:  ▸ [3] Toyota | Corolla | 1234 ABC | 5,40 L/100km
     *
     * @param v El vehículo a mostrar.
     */
    private void printVehiculo(Vehiculo v) {
        System.out.println(
            GREEN  + "  ▸ " + RESET +
            BOLD   + "[" + v.getId() + "] " + RESET +
            WHITE  + v.getMarca() + RESET +
            " | " + CYAN + v.getModelo() + RESET +
            " | " + v.getMatricula() +
            " | Consumo: " + YELLOW + String.format("%.2f L/100km", v.getConsumoHomologado()) + RESET
        );
    }

    /**
     * Imprime un título de sección con línea separadora.
     * El ancho de la línea se ajusta automáticamente al largo del título.
     *
     * @param titulo Texto del título.
     */
    private void printTitulo(String titulo) {
        System.out.println(
            BOLD + CYAN + "── " + titulo + " " +
            "─".repeat(Math.max(0, 34 - titulo.length())) + RESET
        );
    }

    /** Mensaje de operación completada con éxito (verde con ✔). */
    private void printExito(String msg) {
        System.out.println(GREEN + "✔ " + msg + RESET);
    }

    /** Mensaje de advertencia (amarillo con ⚠). */
    private void printAdvertencia(String msg) {
        System.out.println(YELLOW + "⚠ " + msg + RESET);
    }

    /** Mensaje de error (rojo con ✖). Se imprime en System.err para separarlo del flujo normal. */
    private void printError(String msg) {
        System.err.println(RED + "✖ " + msg + RESET);
    }

    /** Mensaje informativo (blanco con ℹ). */
    private void printInfo(String msg) {
        System.out.println(WHITE + "ℹ " + msg + RESET);
    }
}