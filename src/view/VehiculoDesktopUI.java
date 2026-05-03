package view;

import model.domain.Vehiculo;
import model.service.VehiculoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Interfaz gráfica (Swing) para gestionar vehículos.
 *
 * Equivalente visual de VehiculoConsoleUI: sigue siendo la capa "vista" del
 * patrón MVC y delega toda la lógica de negocio en VehiculoService.
 *
 * Características principales:
 *  - Patrón Singleton: solo puede existir una instancia.
 *  - Ventana principal con filtro desplegable (todos / por ID / por marca)
 *    y una tabla de solo lectura con todos los campos del vehículo.
 *  - Botones "Modificar" y "Eliminar" por fila dentro de la propia tabla.
 *  - Botón global "Dar de alta" en la barra inferior.
 *  - Alta, modificación y baja comparten el mismo formulario modal;
 *    solo cambia el texto/icono del botón de confirmación y si los campos
 *    son editables o no.
 */
public class VehiculoDesktopUI {

    // ── Singleton ─────────────────────────────────────────────────────────
    /** Instancia única de la clase (lazy initialization). */
    private static VehiculoDesktopUI instance = null;

    /** Capa de servicio: gestiona la lógica de negocio y el acceso a datos. */
    private final VehiculoService vehiculoService;

    // ── Componentes de la ventana principal ───────────────────────────────
    /** Ventana (frame) principal de la aplicación. */
    private JFrame ventanaPrincipal;

    /**
     * Modelo de datos de la tabla.
     * DefaultTableModel gestiona filas/columnas y notifica a la JTable
     * automáticamente cuando los datos cambian.
     */
    private DefaultTableModel modeloTabla;

    /** Tabla visual que muestra el listado de vehículos. */
    private JTable tablaVehiculos;

    /**
     * ComboBox para seleccionar el tipo de filtro.
     * Opciones: "Ver todo", "Filtrar por ID", "Filtrar por Marca".
     */
    private JComboBox<String> comboFiltro;

    /** Campo de texto donde el usuario escribe el valor del filtro (ID o Marca). */
    private JTextField campoBusqueda;

    /** Etiqueta que indica qué hay que escribir en campoBusqueda. */
    private JLabel labelBusqueda;

    // ── Constantes de columnas de la tabla ────────────────────────────────
    /**
     * Nombres de columna que se muestran en la cabecera de la tabla.
     * Las columnas "Modificar" y "Eliminar" contendrán botones.
     */
    private static final String[] COLUMNAS = {
        "ID", "Marca", "Modelo", "Matrícula", "Consumo (L/100km)", "Modificar", "Eliminar"
    };

    // ── Índices de columna (facilita mantenimiento si cambian las columnas) ──
    private static final int COL_ID         = 0;
    private static final int COL_MARCA      = 1;
    private static final int COL_MODELO     = 2;
    private static final int COL_MATRICULA  = 3;
    private static final int COL_CONSUMO    = 4;
    private static final int COL_MODIFICAR  = 5;
    private static final int COL_ELIMINAR   = 6;

    // ── Modos del formulario ──────────────────────────────────────────────
    /**
     * Enumerado que distingue en qué modo se abre el formulario compartido.
     * Se usa para adaptar el botón de confirmación y la editabilidad de campos.
     */
    private enum ModoFormulario {
        ALTA,        // Crear nuevo vehículo  → botón verde "✔ Dar de alta"
        MODIFICAR,   // Editar vehículo       → botón azul  "✏ Guardar cambios"
        ELIMINAR     // Ver y confirmar baja  → botón rojo  "🗑 Eliminar"
    }

    // ── Constructor ───────────────────────────────────────────────────────

    /** Constructor privado: garantiza que nadie crea instancias desde fuera. */
    private VehiculoDesktopUI(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // ── Singleton: punto de acceso único ─────────────────────────────────

    /**
     * Devuelve la única instancia de VehiculoDesktopUI.
     * Si todavía no existe la crea (lazy initialization).
     *
     * @param vehiculoService Capa de servicio con la lógica de negocio.
     * @return La instancia única.
     */
    public static VehiculoDesktopUI getInstance(VehiculoService vehiculoService) {
        if (instance == null) {
            instance = new VehiculoDesktopUI(vehiculoService);
        }
        return instance;
    }

    // ── Método principal ──────────────────────────────────────────────────

    /**
     * Construye y muestra la ventana principal.
     * Debe llamarse desde el Event Dispatch Thread (EDT) de Swing para
     * garantizar la seguridad en el acceso a componentes gráficos.
     *
     * Ejemplo de uso desde main():
     *   SwingUtilities.invokeLater(() -> VehiculoDesktopUI.getInstance(service).iniciar());
     */
    public void iniciar() {
        // Aplica el Look & Feel del sistema operativo para que la ventana
        // tenga la apariencia nativa (Windows, macOS, Linux GTK…)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing usa su propio L&F por defecto; no es crítico
            e.printStackTrace();
        }

        construirVentanaPrincipal();
        cargarDatosEnTabla(vehiculoService.findAll()); // Carga inicial: todos los vehículos
        ventanaPrincipal.setVisible(true);
    }

    // ── Construcción de la ventana principal ──────────────────────────────

    /**
     * Construye todos los componentes de la ventana principal y los ensambla.
     * Separa la construcción en métodos auxiliares para mejorar la legibilidad.
     */
    private void construirVentanaPrincipal() {
        // ── Frame principal ──────────────────────────────────────────────
        ventanaPrincipal = new JFrame("Gestión de Vehículos");
        ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaPrincipal.setSize(900, 550);
        ventanaPrincipal.setLocationRelativeTo(null); // Centrada en la pantalla
        ventanaPrincipal.setLayout(new BorderLayout(0, 8));

        // Panel con margen interior para que el contenido no pegue a los bordes
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setBorder(new EmptyBorder(10, 10, 10, 10));
        ventanaPrincipal.setContentPane(contenedor);

        // ── Cabecera con título ──────────────────────────────────────────
        contenedor.add(crearPanelCabecera(), BorderLayout.NORTH);

        // ── Panel central: filtros + tabla ───────────────────────────────
        JPanel panelCentral = new JPanel(new BorderLayout(0, 6));
        panelCentral.add(crearPanelFiltros(), BorderLayout.NORTH);
        panelCentral.add(crearPanelTabla(),   BorderLayout.CENTER);
        contenedor.add(panelCentral, BorderLayout.CENTER);

        // ── Barra inferior con botón de alta ─────────────────────────────
        contenedor.add(crearPanelBotonesGlobales(), BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de cabecera con el título de la aplicación.
     *
     * @return Panel listo para añadir al BorderLayout.NORTH del contenedor.
     */
    private JPanel crearPanelCabecera() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 120, 180)); // Azul corporativo
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel titulo = new JLabel("🚗  Gestión de Vehículos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        panel.add(titulo);

        return panel;
    }

    /**
     * Crea el panel de filtros con el comboBox y el campo de búsqueda.
     *
     * La interacción funciona así:
     *  1. El usuario elige en comboFiltro: "Ver todo", "Filtrar por ID"
     *     o "Filtrar por Marca".
     *  2. Si elige "Ver todo" el campo de texto se oculta.
     *  3. Al pulsar el botón "Buscar" se recarga la tabla con los resultados.
     *
     * @return Panel listo para añadir encima de la tabla.
     */
    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));

        // ── ComboBox de tipo de filtro ───────────────────────────────────
        comboFiltro = new JComboBox<>(new String[]{"Ver todo", "Filtrar por ID", "Filtrar por Marca"});

        // ── Campo de texto para el valor del filtro ──────────────────────
        labelBusqueda = new JLabel("Valor:");
        campoBusqueda = new JTextField(15);
        campoBusqueda.setToolTipText("Escribe el ID o la marca a buscar");

        // ── Botón Buscar ─────────────────────────────────────────────────
        JButton botonBuscar = new JButton("🔍 Buscar");

        // Listener del ComboBox: muestra/oculta el campo de texto según la opción
        comboFiltro.addActionListener(e -> {
            boolean necesitaValor = comboFiltro.getSelectedIndex() != 0; // índice 0 = "Ver todo"
            labelBusqueda.setVisible(necesitaValor);
            campoBusqueda.setVisible(necesitaValor);
            botonBuscar.setVisible(necesitaValor);

            // Si se selecciona "Ver todo", recarga la tabla automáticamente
            if (!necesitaValor) {
                cargarDatosEnTabla(vehiculoService.findAll());
            }
        });

        // Listener del botón Buscar: aplica el filtro seleccionado
        botonBuscar.addActionListener(e -> aplicarFiltro());

        // Permite buscar pulsando ENTER en el campo de texto
        campoBusqueda.addActionListener(e -> aplicarFiltro());

        panel.add(new JLabel("Mostrar:"));
        panel.add(comboFiltro);
        panel.add(labelBusqueda);
        panel.add(campoBusqueda);
        panel.add(botonBuscar);

        // Estado inicial: sin campo de texto (opción "Ver todo" seleccionada)
        labelBusqueda.setVisible(false);
        campoBusqueda.setVisible(false);
        botonBuscar.setVisible(false);

        return panel;
    }

 
    private JPanel crearPanelTabla() {

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda editable directamente
            }
        };

        tablaVehiculos = new JTable(modeloTabla);
        tablaVehiculos.setRowHeight(32);
        tablaVehiculos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVehiculos.getTableHeader().setReorderingAllowed(false);
        tablaVehiculos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Centra el texto de las columnas de datos
        DefaultTableCellRenderer centradorTexto = new DefaultTableCellRenderer();
        centradorTexto.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= COL_CONSUMO; i++) {
            tablaVehiculos.getColumnModel().getColumn(i).setCellRenderer(centradorTexto);
        }

        // Renderizador visual de botones (solo apariencia, sin eventos)
        tablaVehiculos.getColumnModel().getColumn(COL_MODIFICAR)
            .setCellRenderer(new BotonCeldaRenderer("✏ Modificar", new Color(0, 100, 200)));
        tablaVehiculos.getColumnModel().getColumn(COL_ELIMINAR)
            .setCellRenderer(new BotonCeldaRenderer("🗑 Eliminar", new Color(200, 0, 0)));

        tablaVehiculos.getColumnModel().getColumn(COL_MODIFICAR).setPreferredWidth(110);
        tablaVehiculos.getColumnModel().getColumn(COL_ELIMINAR).setPreferredWidth(100);

        /**
         * MouseListener que detecta clics en las columnas de botones.
         * Al hacer clic, convierte la coordenada Y del ratón en un índice
         * de fila y la coordenada X en un índice de columna, luego actúa
         * en consecuencia sin necesitar ningún editor de celdas.
         */
        tablaVehiculos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila   = tablaVehiculos.rowAtPoint(e.getPoint());
                int columna = tablaVehiculos.columnAtPoint(e.getPoint());

                if (fila < 0) return; // Clic fuera de cualquier fila

                if (columna == COL_MODIFICAR) {
                    int id = (int) modeloTabla.getValueAt(fila, COL_ID);
                    abrirFormulario(id, ModoFormulario.MODIFICAR);
                } else if (columna == COL_ELIMINAR) {
                    int id = (int) modeloTabla.getValueAt(fila, COL_ID);
                    abrirFormulario(id, ModoFormulario.ELIMINAR);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaVehiculos);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de vehículos"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel inferior con el botón global "Dar de alta".
     *
     * @return Panel listo para añadir al BorderLayout.SOUTH del contenedor.
     */
    private JPanel crearPanelBotonesGlobales() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton botonAlta = new JButton("✚ Dar de alta");
        botonAlta.setBackground(new Color(0, 150, 80));    // Verde para acción positiva
        botonAlta.setForeground(Color.WHITE);
        botonAlta.setFont(botonAlta.getFont().deriveFont(Font.BOLD));
        botonAlta.setFocusPainted(false);
        botonAlta.setPreferredSize(new Dimension(140, 34));

        // Al pulsar el botón se abre el formulario en modo ALTA (sin vehículo previo)
        botonAlta.addActionListener(e -> abrirFormulario(-1, ModoFormulario.ALTA));

        panel.add(botonAlta);
        return panel;
    }

    // ── Lógica de filtrado ────────────────────────────────────────────────

    /**
     * Lee el filtro seleccionado en el ComboBox y recarga la tabla con los
     * resultados correspondientes. Muestra un mensaje de error si el valor
     * del filtro es inválido (p. ej., si se escribe texto donde se espera un ID).
     */
    private void aplicarFiltro() {
        String valorBusqueda = campoBusqueda.getText().trim();

        switch (comboFiltro.getSelectedIndex()) {
            case 0 -> // "Ver todo"
                cargarDatosEnTabla(vehiculoService.findAll());

            case 1 -> { // "Filtrar por ID"
                try {
                    int id = Integer.parseInt(valorBusqueda);
                    Optional<Vehiculo> resultado = vehiculoService.findById(id);
                    ArrayList<Vehiculo> lista = new ArrayList<>();
                    resultado.ifPresent(lista::add); // Añade el vehículo si existe
                    cargarDatosEnTabla(lista);

                    if (lista.isEmpty()) {
                        mostrarMensaje("No se encontró ningún vehículo con ID " + id + ".", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    mostrarMensaje("El ID debe ser un número entero (ej: 3).", "Valor no válido", JOptionPane.WARNING_MESSAGE);
                }
            }

            case 2 -> { // "Filtrar por Marca"
                if (valorBusqueda.isBlank()) {
                    mostrarMensaje("Introduce una marca o fragmento de texto para buscar.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ArrayList<Vehiculo> resultados = vehiculoService.findByMarca(valorBusqueda);
                cargarDatosEnTabla(resultados);

                if (resultados.isEmpty()) {
                    mostrarMensaje("No se encontraron vehículos con la marca \"" + valorBusqueda + "\".","Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    // ── Carga de datos en la tabla ────────────────────────────────────────

    /**
     * Borra el contenido actual de la tabla y la rellena con la lista recibida.
     * Cada vehículo ocupa una fila; las columnas de botones se dejan con
     * texto vacío porque el renderizador/editor los dibuja como JButton.
     *
     * @param vehiculos Lista de vehículos a mostrar (puede estar vacía).
     */
    private void cargarDatosEnTabla(ArrayList<Vehiculo> vehiculos) {
        modeloTabla.setRowCount(0); // Limpia todas las filas anteriores

        for (Vehiculo v : vehiculos) {
            modeloTabla.addRow(new Object[]{
                v.getId(),
                v.getMarca(),
                v.getModelo(),
                v.getMatricula(),
                String.format("%.2f", v.getConsumoHomologado()),
                "✏ Modificar",  // Texto que mostrará el renderizador del botón
                "🗑 Eliminar"   // Texto que mostrará el renderizador del botón
            });
        }
    }

    // ── Formulario compartido (Alta / Modificación / Baja) ────────────────

    /**
     * Abre el formulario modal que se usa para ALTA, MODIFICACIÓN y ELIMINACIÓN.
     *
     * Dependiendo del modo:
     *  - ALTA:      campos vacíos, editables, botón verde "✚ Dar de alta".
     *  - MODIFICAR: campos rellenos con los datos actuales, editables,
     *               botón azul "✏ Guardar cambios".
     *  - ELIMINAR:  campos rellenos, NO editables, botón rojo "🗑 Confirmar eliminación".
     *
     * @param idVehiculo ID del vehículo a cargar (ignorado en modo ALTA; usa -1).
     * @param modo       Modo en que se abre el formulario (ALTA / MODIFICAR / ELIMINAR).
     */
    private void abrirFormulario(int idVehiculo, ModoFormulario modo) {
        // ── Obtener vehículo existente (solo en MODIFICAR y ELIMINAR) ────
        Vehiculo vehiculo = new Vehiculo(); // Objeto vacío para modo ALTA

        if (modo != ModoFormulario.ALTA) {
            Optional<Vehiculo> existente = vehiculoService.findById(idVehiculo);
            if (existente.isEmpty()) {
                mostrarMensaje("No se encontró el vehículo con ID " + idVehiculo + ".",
                               "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            vehiculo = existente.get();
        }

        // ── Diálogo modal ────────────────────────────────────────────────
        JDialog dialogo = new JDialog(ventanaPrincipal, tituloPorModo(modo), true); // true = modal
        dialogo.setSize(420, 320);
        dialogo.setLocationRelativeTo(ventanaPrincipal);
        dialogo.setLayout(new BorderLayout(0, 8));
        dialogo.setResizable(false);

        // ── Panel del formulario con GridBagLayout ────────────────────────
        // GridBagLayout da más control que GridLayout para alinear etiquetas
        // y campos de texto de distintos tamaños.
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos de texto del formulario
        JTextField campoMarca     = new JTextField(vehiculo.getMarca()     != null ? vehiculo.getMarca()     : "", 20);
        JTextField campoModelo    = new JTextField(vehiculo.getModelo()    != null ? vehiculo.getModelo()    : "", 20);
        JTextField campoMatricula = new JTextField(vehiculo.getMatricula() != null ? vehiculo.getMatricula() : "", 20);
        JTextField campoConsumo   = new JTextField(
            vehiculo.getConsumoHomologado() > 0
                ? String.valueOf(vehiculo.getConsumoHomologado())
                : "",
            20
        );

        // En modo ELIMINAR los campos son de solo lectura (solo visualización)
        boolean editable = (modo != ModoFormulario.ELIMINAR);
        campoMarca.setEditable(editable);
        campoModelo.setEditable(editable);
        campoMatricula.setEditable(editable);
        campoConsumo.setEditable(editable);

        // Si no son editables se aplica un fondo gris suave para indicarlo visualmente
        if (!editable) {
            Color fondo = new Color(235, 235, 235);
            campoMarca.setBackground(fondo);
            campoModelo.setBackground(fondo);
            campoMatricula.setBackground(fondo);
            campoConsumo.setBackground(fondo);
        }

        // ── Añadir filas al formulario con GridBagLayout ─────────────────
        añadirFilaFormulario(panelFormulario, gbc, "Marca:",      campoMarca,     0);
        añadirFilaFormulario(panelFormulario, gbc, "Modelo:",     campoModelo,    1);
        añadirFilaFormulario(panelFormulario, gbc, "Matrícula:",  campoMatricula, 2);
        añadirFilaFormulario(panelFormulario, gbc, "Consumo (L/100km):", campoConsumo, 3);

        dialogo.add(panelFormulario, BorderLayout.CENTER);

        // ── Panel de botones del diálogo ─────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));

        // Botón Cancelar: cierra el diálogo sin hacer nada
        JButton botonCancelar = new JButton("✖ Cancelar");
        botonCancelar.addActionListener(e -> dialogo.dispose());

        // Botón de confirmación: su texto, color e icono dependen del modo
        JButton botonConfirmar = crearBotonConfirmar(modo);

        // Referencia final al vehículo para usarla dentro del lambda
        final Vehiculo vehiculoFinal = vehiculo;

        // Listener del botón de confirmación: ejecuta la operación correspondiente
        botonConfirmar.addActionListener(e -> {
            boolean exito = procesarFormulario(
                dialogo, modo, vehiculoFinal,
                campoMarca.getText().trim(),
                campoModelo.getText().trim(),
                campoMatricula.getText().trim(),
                campoConsumo.getText().trim().replace(",", ".")
            );

            if (exito) {
                dialogo.dispose();                         // Cierra el diálogo
                cargarDatosEnTabla(vehiculoService.findAll()); // Refresca la tabla
            }
        });

        panelBotones.add(botonCancelar);
        panelBotones.add(botonConfirmar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setVisible(true); // Bloquea hasta que el usuario cierre el diálogo (es modal)
    }

    /**
     * Añade una fila "etiqueta + campo" al formulario usando GridBagLayout.
     *
     * @param panel     Panel al que se añaden los componentes.
     * @param gbc       Restricciones de GridBagLayout (se modifican internamente).
     * @param etiqueta  Texto de la etiqueta (p. ej., "Marca:").
     * @param campo     Campo de texto asociado a la etiqueta.
     * @param fila      Índice de fila (gridY) donde se coloca en la cuadrícula.
     */
    private void añadirFilaFormulario(JPanel panel, GridBagConstraints gbc,
                                       String etiqueta, JTextField campo, int fila) {
        // Etiqueta en la columna 0
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.3; // Ocupa el 30% del ancho disponible
        panel.add(new JLabel(etiqueta), gbc);

        // Campo de texto en la columna 1
        gbc.gridx = 1;
        gbc.weightx = 0.7; // Ocupa el 70% restante
        panel.add(campo, gbc);
    }

    /**
     * Crea el botón de confirmación con el texto, color e icono adecuados
     * al modo del formulario:
     *  - ALTA:      fondo verde,  "✚ Dar de alta"
     *  - MODIFICAR: fondo azul,   "✏ Guardar cambios"
     *  - ELIMINAR:  fondo rojo,   "🗑 Confirmar eliminación"
     *
     * @param modo Modo del formulario.
     * @return El JButton configurado.
     */
    private JButton crearBotonConfirmar(ModoFormulario modo) {
        String texto;
        Color color;

        switch (modo) {
            case ALTA      -> { texto = "✚ Dar de alta";           color = new Color(0, 150, 80);  }
            case MODIFICAR -> { texto = "✏ Guardar cambios";       color = new Color(0, 100, 200); }
            case ELIMINAR  -> { texto = "🗑 Confirmar eliminación"; color = new Color(200, 0, 0);   }
            default        -> { texto = "Confirmar";               color = Color.GRAY;             }
        }

        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setFocusPainted(false);
        boton.setOpaque(true);           // Necesario en macOS para que se aplique el color de fondo
        boton.setBorderPainted(false);   // Elimina el borde para que el color se vea limpio

        return boton;
    }

    /**
     * Ejecuta la operación de negocio correspondiente al modo del formulario.
     * Valida los campos antes de llamar al servicio y muestra mensajes de éxito/error.
     *
     * @param dialogo     Diálogo modal del formulario (para posicionar los mensajes).
     * @param modo        Modo del formulario (ALTA / MODIFICAR / ELIMINAR).
     * @param vehiculo    Vehículo original (solo relevante en MODIFICAR y ELIMINAR).
     * @param marca       Valor introducido en el campo Marca.
     * @param modelo      Valor introducido en el campo Modelo.
     * @param matricula   Valor introducido en el campo Matrícula.
     * @param consumoStr  Valor introducido en el campo Consumo (como String).
     * @return true si la operación tuvo éxito; false si se produjo un error.
     */
    private boolean procesarFormulario(JDialog dialogo, ModoFormulario modo,
                                        Vehiculo vehiculo,
                                        String marca, String modelo,
                                        String matricula, String consumoStr) {
        try {
            switch (modo) {
                case ALTA -> {
                    // Validar que ningún campo esté vacío antes de crear
                    if (marca.isBlank() || modelo.isBlank() || matricula.isBlank() || consumoStr.isBlank()) {
                        mostrarMensajeEnDialogo(dialogo,
                            "Todos los campos son obligatorios.",
                            "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    Vehiculo nuevo = new Vehiculo();
                    nuevo.setMarca(marca);
                    nuevo.setModelo(modelo);
                    nuevo.setMatricula(matricula);
                    nuevo.setConsumoHomologado(Double.parseDouble(consumoStr));

                    vehiculoService.create(nuevo);
                    mostrarMensaje("Vehículo dado de alta correctamente.", "Alta exitosa",
                                   JOptionPane.INFORMATION_MESSAGE);
                }

                case MODIFICAR -> {
                    // Solo actualizamos los campos que el usuario haya rellenado;
                    // si dejó un campo vacío se conserva el valor anterior
                    if (!marca.isBlank())     vehiculo.setMarca(marca);
                    if (!modelo.isBlank())    vehiculo.setModelo(modelo);
                    if (!matricula.isBlank()) vehiculo.setMatricula(matricula);
                    if (!consumoStr.isBlank()) {
                        vehiculo.setConsumoHomologado(Double.parseDouble(consumoStr));
                    }

                    vehiculoService.update(vehiculo);
                    mostrarMensaje("Vehículo actualizado correctamente.", "Modificación exitosa",
                                   JOptionPane.INFORMATION_MESSAGE);
                }

                case ELIMINAR -> {
                    // Pedir confirmación adicional antes de borrar (acción irreversible)
                    int respuesta = JOptionPane.showConfirmDialog(
                        dialogo,
                        "¿Estás seguro de que deseas eliminar este vehículo?\nEsta acción no se puede deshacer.",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );

                    if (respuesta != JOptionPane.YES_OPTION) {
                        return false; // El usuario canceló: no cerramos el diálogo
                    }

                    vehiculoService.deleteById(vehiculo.getId());
                    mostrarMensaje("Vehículo eliminado correctamente.", "Baja exitosa",
                                   JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return true; // Operación completada sin errores

        } catch (NumberFormatException ex) {
            // El campo Consumo tenía un valor que no es un número decimal válido
            mostrarMensajeEnDialogo(dialogo,
                "El consumo debe ser un número decimal válido (ej: 6.4 o 6,4).",
                "Valor no válido", JOptionPane.ERROR_MESSAGE);
            return false;

        } catch (IllegalArgumentException ex) {
            // VehiculoService lanza esta excepción si algún campo no pasa la validación
            mostrarMensajeEnDialogo(dialogo,
                "Error de validación: " + ex.getMessage(),
                "Datos no válidos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ── Helpers de texto y mensajes ───────────────────────────────────────

    /**
     * Devuelve el título del diálogo según el modo del formulario.
     *
     * @param modo Modo del formulario.
     * @return Título descriptivo para la barra del JDialog.
     */
    private String tituloPorModo(ModoFormulario modo) {
        return switch (modo) {
            case ALTA      -> "Dar de alta un vehículo";
            case MODIFICAR -> "Modificar vehículo";
            case ELIMINAR  -> "Eliminar vehículo";
        };
    }

    /**
     * Muestra un JOptionPane centrado en la ventana principal.
     *
     * @param mensaje   Texto del mensaje.
     * @param titulo    Título de la ventana del mensaje.
     * @param tipo      Tipo de icono (JOptionPane.INFORMATION_MESSAGE, etc.).
     */
    private void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(ventanaPrincipal, mensaje, titulo, tipo);
    }

    /**
     * Muestra un JOptionPane centrado en un diálogo hijo (el formulario).
     *
     * @param padre   Diálogo que actúa como padre del mensaje.
     * @param mensaje Texto del mensaje.
     * @param titulo  Título de la ventana del mensaje.
     * @param tipo    Tipo de icono.
     */
    private void mostrarMensajeEnDialogo(JDialog padre, String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CLASES INTERNAS: Renderizador y Editor de botones en celdas de tabla
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Renderizador de celda que dibuja un JButton dentro de una celda de JTable.
     *
     * En Swing, el renderizador no maneja eventos: solo define la apariencia
     * visual de la celda. Los eventos del ratón los gestiona el editor (ver abajo).
     *
     * Implementa TableCellRenderer para poder asignarse con
     * JTable.getColumnModel().getColumn(n).setCellRenderer(...).
     */
    private static class BotonCeldaRenderer implements javax.swing.table.TableCellRenderer {
        /** El JButton que se "pinta" dentro de la celda. */
        private final JButton boton;

        /**
         * @param texto Etiqueta del botón (puede incluir emoji como icono).
         * @param color Color de fondo del botón.
         */
        BotonCeldaRenderer(String texto, Color color) {
            boton = new JButton(texto);
            boton.setBackground(color);
            boton.setForeground(Color.WHITE);
            boton.setFont(boton.getFont().deriveFont(Font.BOLD, 11f));
            boton.setFocusPainted(false);
            boton.setOpaque(true);
            boton.setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            // El renderizador siempre devuelve el mismo botón configurado
            return boton;
        }
    }

}
