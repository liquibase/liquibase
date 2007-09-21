package liquibase.database.template;

/**
 * Subinterface of SqlTypeValue that adds a cleanup callback,
 * to be invoked after the value has been set and the corresponding
 * statement has been executed.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public interface DisposableSqlTypeValue extends SqlTypeValue {

	/**
	 * Clean up resources held by this type value,
	 * for example the LobCreator in case of a SqlLobValue.
	 */
	void cleanup();

}
