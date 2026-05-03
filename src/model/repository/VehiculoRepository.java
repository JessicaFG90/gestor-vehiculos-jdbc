package model.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

import model.domain.Vehiculo;

public class VehiculoRepository {
	
	private static VehiculoRepository instance = null;

	public static VehiculoRepository getInstance(String url, String user, String password) {
		if (instance == null) {
			instance = new VehiculoRepository(url, user, password);
		}

		return instance;
	}

	private String url;
	private String user;
	private String password;

	private VehiculoRepository(String url, String user, String password) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
	}

	//Creación de la conexión con la BDD.
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	//Método para crear un nuevo vehículo en la BDD.
	public void create(Vehiculo vehiculo) {
		String sql = "INSERT INTO `m0495_prg_evaluable06`.`vehiculo` (`marca`,`modelo`,`matricula`, `consumoHomologado`) VALUES (?, ?, ?, ?);";
		try (Connection connection = getConnection();) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, vehiculo.getMarca());
			preparedStatement.setString(2, vehiculo.getModelo());
			preparedStatement.setString(3, vehiculo.getMatricula());
			preparedStatement.setDouble(4, vehiculo.getConsumoHomologado());
			preparedStatement.executeUpdate();
			connection.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	//Método para actualizar/modificar los datos de un vehículo en la BDD.
	public void update(Vehiculo vehiculo) {
		String sql = "UPDATE `m0495_prg_evaluable06`.`vehiculo` SET marca = ?, modelo = ?, matricula = ?, consumoHomologado = ? WHERE id = ?;";

		try (Connection connection = getConnection();) {

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, vehiculo.getMarca());
			preparedStatement.setString(2, vehiculo.getModelo());
			preparedStatement.setString(3, vehiculo.getMatricula());
			preparedStatement.setDouble(4, vehiculo.getConsumoHomologado());
			preparedStatement.setInt(5, vehiculo.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	//Método para buscar todos los vehículos registrados en la BDD.
	public ArrayList<Vehiculo> findAll() {
		ArrayList<Vehiculo> resultado = new ArrayList<Vehiculo>();
		String sql = "SELECT * from Vehiculo;";

		try (Connection connection = getConnection();) {
			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				Vehiculo vehiculo = this.getVehiculoByResultSet(resultSet);
				resultado.add(vehiculo);
			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return resultado;
	}

	//Método para buscar vehículos por ID en la BDD.
	public Optional<Vehiculo> findById(int id) {
		Optional<Vehiculo> resultado = Optional.empty();

		String sql = "SELECT * from Vehiculo WHERE id = ?";

		try (Connection connection = this.getConnection();) {

			PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.first())
			{
				Vehiculo vehiculo = this.getVehiculoByResultSet(resultSet);
				resultado = Optional.of(vehiculo);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return resultado;
	}

	//Método para buscar vehículos por marca en la BDD.
	public ArrayList<Vehiculo> findByMarca(String marca) {
		ArrayList<Vehiculo> resultado = new ArrayList<Vehiculo>();

		String sql = "SELECT * from Vehiculo WHERE marca like ?";

		try (Connection connection = this.getConnection();) {

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, "%" + marca + "%");
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Vehiculo vehiculo = this.getVehiculoByResultSet(resultSet);
				resultado.add(vehiculo);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return resultado;
	}

	//Método para eliminar vehículos por ID en la BDD.
	public void deleteById(int id) {
		String sql = "DELETE FROM vehiculo WHERE id = ?";

		try (Connection connection = getConnection();) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	//Mapeo de los datos de la consulta SQL a un objeto Vehiculo.
	private Vehiculo getVehiculoByResultSet(ResultSet resultSet) throws SQLException {
		Vehiculo vehiculo = new Vehiculo();

		vehiculo.setId(resultSet.getInt(1));
		vehiculo.setMarca(resultSet.getString(2));
		vehiculo.setModelo(resultSet.getString(3));
		vehiculo.setMatricula(resultSet.getString(4));
		vehiculo.setConsumoHomologado(resultSet.getDouble(5));

		return vehiculo;
	}

}
