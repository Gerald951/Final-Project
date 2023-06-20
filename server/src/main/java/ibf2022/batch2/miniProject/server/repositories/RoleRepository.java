package ibf2022.batch2.miniProject.server.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import ibf2022.batch2.miniProject.server.model.ERole;
import ibf2022.batch2.miniProject.server.model.Role;

@Repository
public class RoleRepository {

  private final static String FIND_NAME = "select * from roles where name=?";
  
  @Autowired
  @Qualifier("jdbcTemplate2")
  private JdbcTemplate jdbcTemplate2;

  public Optional<Role> findByName(ERole name) {
    String roleStr = name.toString();
    Optional<Role> role = jdbcTemplate2.query(FIND_NAME, new ResultSetExtractor<Optional<Role>>() {
      @Override
      public Optional<Role> extractData(ResultSet rs) throws SQLException {
        if (rs.next()) {
          Role role = new Role();
          role.setId(rs.getInt("id"));
          role.setName(ERole.valueOf(rs.getString("name")));
          return Optional.of(role);
        } else {
          return Optional.empty();
        }
      }
    }, roleStr);

    return role;
  }
}
