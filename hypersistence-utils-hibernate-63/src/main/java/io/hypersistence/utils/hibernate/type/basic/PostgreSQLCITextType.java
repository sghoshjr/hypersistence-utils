package io.hypersistence.utils.hibernate.type.basic;

import io.hypersistence.utils.hibernate.type.ImmutableType;
import io.hypersistence.utils.hibernate.type.util.Configuration;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Maps a {@link String} object type to a PostgreSQL <a href="https://www.postgresql.org/docs/current/citext.html">citext</a>
 * column type.
 *
 * @author Sergei Portnov
 */
public class PostgreSQLCITextType extends ImmutableType<String> {

    public static final PostgreSQLCITextType INSTANCE = new PostgreSQLCITextType();

    public PostgreSQLCITextType() {
        super(String.class);
    }

    public PostgreSQLCITextType(org.hibernate.type.spi.TypeBootstrapContext typeBootstrapContext) {
        super(String.class, new Configuration(typeBootstrapContext.getConfigurationSettings()));
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    protected String get(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object value = rs.getObject(position);
        return value == null ? null : value.toString();
    }

    @Override
    protected void set(PreparedStatement st, String value, int index, SharedSessionContractImplementor session) throws SQLException {
        st.setObject(index, value, Types.OTHER);
    }

    @Override
    public String fromStringValue(CharSequence sequence) throws HibernateException {
        return (String) sequence;
    }
}
