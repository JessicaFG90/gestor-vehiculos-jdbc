package controller;

import model.configuration.MiConfiguracion;
import model.service.VehiculoService;
import view.VehiculoConsoleUI;
import view.VehiculoDesktopUI;

public class Main {

	private static String URL = MiConfiguracion.getInstance().getUrl();
	private static String USER = MiConfiguracion.getInstance().getUser();
	private static String PASSWORD = MiConfiguracion.getInstance().getPassword();
	private static String UI = MiConfiguracion.getInstance().getUI();

	public static void main(String[] args) {
		
		VehiculoService vehiculoService = VehiculoService.getInstance(URL, USER, PASSWORD);
		
		if (UI.equals("console")) {
			VehiculoConsoleUI.getInstance(vehiculoService).iniciar();
		} else if (UI.equals("desktop")){
			VehiculoDesktopUI.getInstance(vehiculoService).iniciar();
		} else {
			System.err.println("UI no definida");
		}
	}

}
