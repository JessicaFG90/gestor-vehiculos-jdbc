package model.service;

import java.util.ArrayList;
import java.util.Optional;

import model.domain.Vehiculo;
import model.repository.VehiculoRepository;

/*Una clase de servicio, habitualmente es aquella clase que contiene la lógica de la programación. Se aplica la lógica de validaciones (por ejemplo, comprobar que no dejen campos en blanco,
  que el consumo homologado no es negativo, etc.). Es la capa intermedia entre lo que ve el usuario (el Main) y la base de datos (VehiculoRepository en este caso).*/

public class VehiculoService {
	
    private static VehiculoService instance = null;
    private VehiculoRepository vehiculoRepository;

    private VehiculoService(String url, String user, String password) {
        this.vehiculoRepository = VehiculoRepository.getInstance(url, user, password);
    }

    public static VehiculoService getInstance(String url, String user, String password) {
        if (instance == null) {
            instance = new VehiculoService(url, user, password);
        }
        return instance;
    }

    // --- Validación ---

    private void validarVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getMarca() == null || vehiculo.getMarca().isBlank()) {
            throw new IllegalArgumentException("La marca no puede estar en blanco.");
        }
        if (vehiculo.getModelo() == null || vehiculo.getModelo().isBlank()) {
            throw new IllegalArgumentException("El modelo no puede estar en blanco.");
        }
        if (vehiculo.getMatricula() == null || vehiculo.getMatricula().isBlank()) {
            throw new IllegalArgumentException("El número de matrícula no puede estar en blanco.");
        }
        if (vehiculo.getConsumoHomologado() < 0) {
            throw new IllegalArgumentException("El consumo homologado no puede ser negativo.");
        }
    }

    // --- Métodos públicos ---

    public void create(Vehiculo vehiculo) {
        validarVehiculo(vehiculo);
        vehiculoRepository.create(vehiculo);
    }

    public void update(Vehiculo vehiculo) {
        validarVehiculo(vehiculo);
        vehiculoRepository.update(vehiculo);
    }

    public ArrayList<Vehiculo> findAll() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> findById(int id) {
        return vehiculoRepository.findById(id);
    }

    public ArrayList<Vehiculo> findByMarca(String marca) {
        return vehiculoRepository.findByMarca(marca);
    }

    public void deleteById(int id) {
    	vehiculoRepository.deleteById(id);
    }

}
